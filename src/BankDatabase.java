import java.io.*;
import java.util.Arrays;
import java.util.UUID;

public class BankDatabase implements Serializable {
    private final DynamicList<User> users;
    private final DynamicList<Bank> banks;
    private DynamicList<String[]> securityQuestionInfo;
    private User loggedInUser = null;

    public BankDatabase() {
        this.users = new DynamicList<>("Users");
        this.banks = new DynamicList<>("Banks");
        this.securityQuestionInfo = getSecurityQuestionInfo();
    }

    private DynamicList<String[]> getSecurityQuestionInfo() { // :: !!
        DynamicList<String[]> securityQuestionData = new DynamicList<>("Security Questions Information");

        DynamicList<String> fileLines = Utility.readFileLines("src/SecurityQuestionsOptions.txt");

        for (String line : fileLines) {
            securityQuestionData.add(line.split(","));
        }

        return securityQuestionData;
    }

    public void updateSecurityQuestions() {
        securityQuestionInfo = getSecurityQuestionInfo();
    }

    public int getNumberOfUsers() {
        return users.size();
    }

    public int getNumberOfBanks() {
        return banks.size();
    }

    public void addBank() {
        Utility.printTitle("ADD NEW BANK");

        String name = Utility.getInput("Enter Bank NAME");

        String sortCode;
        do {
            sortCode = Utility.randomOfLength(2) + "-" + Utility.randomOfLength(2) + "-" + Utility.randomOfLength(2);
        } while (!(isValidSortCode(sortCode) && uniqueSortCode(sortCode)));

        double interestRate = Utility.getDoubleInput("Enter Bank INTEREST RATE {XX%}") / 100;

        double transactionCharge = Utility.getDoubleInput("Enter Bank CHARGE PER TRANSACTION {XX%}") / 100;

        double initialBalance = Utility.getDoubleInput("Enter Bank BALANCE");

        Bank bank = new Bank(getNewId(), name, sortCode, interestRate, transactionCharge, initialBalance, new DynamicList<>(Utility.formatPossession(name) + " Accounts"), new DynamicList<>(Utility.formatPossession(name) + " Transactions"));
        banks.add(bank);

        Utility.printSuccess("BANK SUCCESSFULLY ADDED: " + bank);
    }

    public void addUser() {
        boolean valid = false;
        Utility.printTitle("ADD NEW USER");

        Utility.printChapter("USERNAME");
        String username = Utility.getInput("Enter Username {3-20 Characters}");
        if (!username.isEmpty()) {
            if (username.matches("^[a-zA-Z0-9_]{3,20}$")) {
                if (!usernameExistsInDatabase(username)) {

                    Utility.printChapter("PASSWORD");
                    Password validPassword = newPassword("Password", "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&*()-_=+{};:',.<>?/]).{8,}$", 3);

                    Utility.printChapter("SECURITY QUESTIONS");
                    DynamicList<SecurityQuestion> securityQuestions = new DynamicList<>("");
                    DynamicList<String[]> usedSecurityQuestionOutputs = new DynamicList<>("");

                    do {
                        DynamicList<String> securityQuestionsOutputs = new DynamicList<>("Security Questions");
                        for (String[] securityQuestion : securityQuestionInfo) {
                            if (!usedSecurityQuestionOutputs.contains(securityQuestion))
                                securityQuestionsOutputs.add(securityQuestion[0]);
                        }

                        int choice = Utility.getIntInput("Choose a Security Question " + (securityQuestions.size() + 1) + "/3", securityQuestionsOutputs);
                        String[] chosenSecurityQuestion = new String[2];
                        for (String[] s : securityQuestionInfo) {
                            if (s[0].equals(securityQuestionsOutputs.get(choice))) chosenSecurityQuestion = s;
                        }

                        Password answer = newPassword(chosenSecurityQuestion[0], chosenSecurityQuestion[1], 5);
                        SecurityQuestion securityQuestion = new SecurityQuestion(getNewId(), chosenSecurityQuestion[0], chosenSecurityQuestion[1], answer);
                        securityQuestions.add(securityQuestion);
                        usedSecurityQuestionOutputs.add(chosenSecurityQuestion);
                    } while (securityQuestions.size() < 3);

                    Utility.printChapter("INFORMATION");
                    String title;
                    do {
                        title = Utility.getInput("Enter Honorific");
                    } while (!title.matches("^[a-zA-Z ]{2,10}$"));

                    String fullName;
                    do {
                        fullName = Utility.getInput("Enter Full Name");
                    } while (!(fullName + " ").matches("^([A-Z][a-z]+ ){2,}$"));

                    User user;
                    do {
                        user = new User(
                                getNewId(),
                                username,
                                validPassword,
                                securityQuestions,
                                title,
                                fullName,

                                new DynamicList<>(""),
                                new DynamicList<>(""),
                                Utility.now(),
                                0
                        );
                    } while (notUniqueId(user.getId(), users));

                    valid = true;
                    users.add(user);
                    Utility.printSuccess("USER SUCCESSFULLY ADDED: " + user);

                } else Utility.printLog("USERNAME already exists");
            } else Utility.printLog("USERNAME incorrectly formatted!");
        } else valid = true; // Exit if username left empty
        // Re-call if not valid
        if (!valid) addUser();
    }

    private boolean usernameExistsInDatabase(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) return true;
        }
        return false;
    }

    private Password newPassword(String output, String regex, int maxAttempts) {
        char[] tempPassword = Utility.getPassword("Enter " + output);
        if (new String(tempPassword).matches(regex)) {
            char[] confirmPassword = Utility.getPassword("Confirm " + output);

            // Only return if passwords were alike
            if (!Arrays.equals(tempPassword, confirmPassword)) {
                Utility.printError("Passwords were not the same!");
                // Clear all memory of passwords
                Arrays.fill(tempPassword, '\0');
                Arrays.fill(confirmPassword, '\0');
                return newPassword(output, regex, maxAttempts);
            } else {
                Password password = new Password(tempPassword, maxAttempts);
                // Clear all memory of passwords
                Arrays.fill(tempPassword, '\0');
                Arrays.fill(confirmPassword, '\0');
                return password;
            }
        } else {
            Utility.printLog("'%d' Incorrectly formatted!", output);
            // Clear all memory of password
            Arrays.fill(tempPassword, '\0');
            return newPassword(output, regex, maxAttempts);
        }
    }

    public void removeUser() {
        Utility.printTitle("REMOVE USER");

        User user = users.get(Utility.getIntInput("Which user?", users));
        if (Utility.getIntInput("Are you sure?", new DynamicList<>("Delete " + user + "?", new String[] {"Yes", "No"})) == 0) {
            users.remove(users.indexOf(user));
            Utility.printSuccess(user + " has been removed!");
        } else {
            Utility.printLog("Cancelled!");
        }
    }

    public void login() {
        Utility.printTitle("USER LOGIN");


        loggedInUser = getUserFromCredentials();

        if (loggedInUser != null) {
            Utility.printSuccess("LOGGED IN AS " + loggedInUser.getUsername().toUpperCase());
        } else {
            Utility.printError("NOT LOGGED IN");
        }
    }

    private User getUserFromCredentials() {
        User targetUser = null;

        String username = Utility.getInput("Enter username");
        if (!username.isEmpty()) {
            for (User user : users) {
                if (username.equals(user.getUsername())) {
                    targetUser = user;
                    break;
                }
            }

            if (targetUser != null) {
                if (targetUser.isUnlocked()) {
                    targetUser.resetPasswordAttempts();
                    if (targetUser.checkASecurityQuestion()) {
                        while (targetUser.checkAttempts()) {
                            char[] attemptedPassword = Utility.getPassword("Enter Password");

                            if (targetUser.checkPassword(attemptedPassword)) {
                                targetUser.resetNumberOfTimesLocked();
                                return targetUser;
                            } else {
                                Utility.printLog("Password Incorrect!");
                            }
                            // Clear from memory
                            Arrays.fill(attemptedPassword, '\0');
                        }
                        // Answer Incorrectly Too Many Times:
                        targetUser.lockUser();
                        return null;
                    } else Utility.printLog("Security Question Failed!");
                } else {
                    Utility.printError(targetUser);
                    return null;
                }
            } else Utility.printLog("No User found with username '%d'", username);
        } else return null;
        return getUserFromCredentials();
    }

    public void logout() {
        Utility.printTitle("LOGGING '" + loggedInUser.getUsername() + "' OUT");

        loggedInUser = null;
    }

    public void addAccount() {
        boolean valid = false;
        Utility.printTitle("ADD ACCOUNT");

        DynamicList<AccountType> types = AccountType.getAllAccountTypes();
        AccountType type = types.get(Utility.getIntInput("Enter Account Type", types));

        DynamicList<User> accountUsers = new DynamicList<>("Users");
        accountUsers.add(loggedInUser);
        if (type.isJoint()) {
            Utility.printChapter("ADD ADDITIONAL USERS:");
            do {
                User newUser = getUserFromCredentials();
                if (newUser != null) {
                    if (!accountUsers.contains(newUser)) {
                        accountUsers.add(newUser);
                    } else Utility.printError("User '" + newUser + "' is already a user in the Account");
                }
            } while (accountUsers.size() < 2 || Utility.getIntInput("Add Another User?", new DynamicList<>("Options", new String[]{"Yes", "No"})) == 0);
        }

        String number = String.valueOf(Utility.randomOfLength(8));
        if (isValidAccountNumber(number)) {
            if (!accountExistsInDatabase(number)) {

                Bank bank = banks.get(Utility.getIntInput("Enter Bank", banks));
                String sortCode = bank.getSortCode();


                if (!loggedInUser.accountTypeExists(type.toString())) {

                    double initialBalance;
                    do {
                        initialBalance = Utility.getDoubleInput("Enter Initial Balance");
                    } while (!bank.canAfford(initialBalance));

                    Account account;
                    do {
                        account = new Account(getNewId(), type, number, sortCode, initialBalance, new DynamicList<>(""), accountUsers, Utility.now());
                    } while (notUniqueId(account.getId(), loggedInUser.getAccounts()));

                    valid = true;

                    for (User user : accountUsers) {
                        user.addAccount(account);
                    }

                    bank.addAccount(account);
                    bank.changeMoney(-1 * initialBalance);

                    if (bank.checkBalance()) {
                        Utility.printSuccess("ACCOUNT SUCCESSFULLY CREATED: " + account);
                    } else {
                        Utility.printError("Account addition error!");
                    }

                } else Utility.printLog("You already have an Account with type '%d'!", type);
            } else Utility.printError("Account NUMBER Invalid!");
        } else Utility.printError("Account NUMBER incorrectly formatted!");
        if (!valid) addAccount();
    }

    private boolean accountExistsInDatabase(String number) {
        for (Account account : loggedInUser.getAccounts()) {
            if (account.checkNumber(number)) return true;
        }
        return false;
    }

    public void viewUser() {
        Utility.printTitle("VIEW USER INFO");

        Utility.print(loggedInUser.toInfo());
    }

    public void viewAccounts() {
        Utility.printTitle("VIEW ACCOUNTS INFO");

        DynamicList<Account> accounts = loggedInUser.getAccounts();
        for (int i = 0; i < accounts.size(); i++) {
            Utility.print(accounts.get(i).toInfo());
        }
    }

    public void changeAccountStatus() {
        Account targetAccount = loggedInUser.getAccounts().get(Utility.getIntInput("Enter Account", loggedInUser.getAccounts()));

        DynamicList<Status> statuses = Status.getAllStatuses();
        Status status = statuses.get(Utility.getIntInput("Enter new STATUS for " + targetAccount, statuses));

        if (!status.equals(Status.CLOSED) || targetAccount.isEmpty()) {
            targetAccount.setStatus(status);
        } else Utility.printError("Account '" + targetAccount + "' is not empty!");
    }

    public void makeInternalTransaction() {
        Utility.printTitle("MAKE INTERNAL TRANSACTION");
        DynamicList<Account> accounts = loggedInUser.getAccounts();
        Account fromAccount, toAccount;

        if (accounts.size() >= 2) {
            int fromChoice = Utility.getIntInput("Enter FROM Account", accounts);
            fromAccount = accounts.get(fromChoice);

            DynamicList<Account> remainingAccounts = accounts.clone();
            remainingAccounts.remove(fromChoice);
            if (remainingAccounts.size() > 1) {
                int toChoice = Utility.getIntInput("Enter TO Account", remainingAccounts);
                toAccount = remainingAccounts.get(toChoice);
            } else toAccount = remainingAccounts.get(0);

            String reference = Utility.getInput("Enter REFERENCE [OPTIONAL]");

            performTransfer(fromAccount, toAccount, reference, false);

        } else Utility.printLog("Not Enough Accounts (%d/2)!", accounts.size());
    }

    public void makeExternalTransaction() {
        Utility.printTitle("MAKE EXTERNAL TRANSACTION");
        DynamicList<Connection> connectedAccounts = loggedInUser.getConnectedAccounts();
        DynamicList<Account> accounts = loggedInUser.getAccounts();
        Account toAccount, fromAccount;

        int fromChoice = Utility.getIntInput("Enter FROM Account", accounts);
        fromAccount = accounts.get(fromChoice);

        if (connectedAccounts.isEmpty()) {
            toAccount = addConnectedAccount(fromAccount);
        } else {
            DynamicList<String> options = new DynamicList<>("Transfer TO");
            options.add("Existing Account");
            options.add("New Account");

            int choice = Utility.getIntInput("Enter choice", options);

            if (options.get(choice).equals("Existing Account")) {
                int toChoice = Utility.getIntInput("Enter TO Account", connectedAccounts);
                toAccount = connectedAccounts.get(toChoice).getToAccount();
            } else {
                toAccount = addConnectedAccount(fromAccount);
            }
        }

        Connection connection = getConnection(toAccount);
        if (connection != null) {
            String reference = Utility.getInput("Enter REFERENCE [Default: '" + connection.getReference() + "']");

            if (loggedInUser.checkASecurityQuestion()) {
                if (reference.isEmpty()) performTransfer(fromAccount, toAccount, connection.getReference(), true);
                else performTransfer(fromAccount, toAccount, reference, true);
            } Utility.printLog("Security Question Failed!");
        } else Utility.printError("No Connection Found! Between " + loggedInUser + " and " + toAccount + "!");
    }

    private Connection getConnection(Account account) {
        for (Connection connection : loggedInUser.getConnectedAccounts()) {
            if (connection.getToAccount() == account) return connection;
        }
        return null;
    }

    private void performTransfer(Account fromAccount, Account toAccount, String reference, Boolean chargeAccount) {
        Utility.printLog("Transferring FROM %d TO %d.", fromAccount.formatName(), toAccount.formatName());

        Bank bank = getBank(fromAccount);

        if (bank != null) {

            double amount = Utility.getDoubleInput("Enter AMOUNT");
            double charge;
            if (chargeAccount) charge = amount * bank.getTransactionCharge();
            else charge = 0;

            // If they can not afford this try again (with message)
            while (!fromAccount.canAfford(amount + charge)) {
                Utility.printError(fromAccount.formatName() + " can NOT afford: " + Utility.formatMoney(amount) + " + " + Utility.formatMoney(charge) + " (CHARGE) = " + Utility.formatMoney(amount + charge));
                amount = Utility.getDoubleInput("Enter AMOUNT");
                if (chargeAccount) charge = amount * bank.getTransactionCharge();
                else charge = 0;
            }

            Utility.printLog("Transferring %d FROM %d TO %d.", Utility.formatMoney(amount), fromAccount.formatName(), toAccount.formatName());

            Transaction transaction;
            if (!reference.isEmpty()) {
                transaction = new Transaction(getNewId(), fromAccount, toAccount, amount, charge, Utility.now(), reference);
            } else transaction = new Transaction(getNewId(), fromAccount, toAccount, amount, charge, Utility.now());

            bank.changeMoney(charge);
            fromAccount.changeBalance(transaction.getAmount(fromAccount));
            toAccount.changeBalance(transaction.getAmount(toAccount));

            bank.addTransaction(transaction);
            fromAccount.addTransaction(transaction);
            toAccount.addTransaction(transaction);

            if (fromAccount.checkBalance() && toAccount.checkBalance() && bank.checkBalance()) {
                Utility.printSuccess("Transaction Successful: " + transaction);
            } else {
                Utility.printError("Transaction ERROR:");
                Utility.printError("FROM CHECK - " + fromAccount.checkBalance());
                Utility.printError("TO CHECK - " + toAccount.checkBalance());
                Utility.printError("BANK CHECK - " + bank.checkBalance());
                // Revert balance to original
            }
        } else Utility.printError("No Bank found for Account '" + fromAccount + "'!");
    }

    private Bank getBank(Account account) {
        for (Bank bank : banks) {
            if (account.checkSortCode(bank.getSortCode())) return bank;
        }
        return null;
    }

    public Account addConnectedAccount(Account fromAccount) {
        boolean valid = false;

        Utility.printTitle("ADD EXTERNAL CONNECTION");
        Account toAccount = null;
        String payeeName = Utility.getInput("Enter PAYEE NAME");
        Utility.print("Checking format...", false);
        if (payeeName.matches("^[A-Z]+ [A-Z] [A-Z]+$")) {
            Utility.print("[PASSED]");
            Utility.print("Checking Occurrences...", false);
            if (checkNumberOfAccounts(payeeName)) {
                Utility.print("[PASSED]");

                String number = Utility.getInput("Enter ACCOUNT NUMBER");
                if (isValidAccountNumber(number)) {

                    String sortCode = Utility.getInput("Enter SORT CODE");
                    if (isValidSortCode(sortCode)) {

                        Utility.print("Checking Credentials...", false);
                        toAccount = getAccountFromCredentials(payeeName, number, sortCode);
                        if (toAccount != null) {
                            if (toAccount != fromAccount) {
                                Utility.print("[PASSED]");
                                String reference = Utility.getInput("Enter Default REFERENCE");

                                if (!connectionExistsInDatabase(toAccount)) {
                                    Connection connection;
                                    if (reference.isEmpty())
                                        connection = new Connection(getNewId(), loggedInUser, toAccount);
                                    else connection = new Connection(getNewId(), loggedInUser, toAccount, reference);

                                    loggedInUser.addConnection(connection);

                                    valid = true;

                                    Utility.printSuccess("CONNECTION SUCCESSFULLY ADDED: " + connection);

                                } else Utility.printLog("Connection already exists");
                            } else Utility.printLog("Accounts can not be equal!");
                        } else Utility.printLog("No Account found!");
                    }
                }
            } else Utility.printLog("No Payee Found!");
        }
        if (!valid) return addConnectedAccount(fromAccount);
        return toAccount;
    }

    private boolean connectionExistsInDatabase(Account toAccount) {
        for (Connection connection : loggedInUser.getConnectedAccounts()) {
            if (connection.getToAccount() == toAccount) return true;
        }
        return false;
    }

    private Account getAccountFromCredentials(String payeeName, String number, String sortCode) {
        for (User user : users) {
            if (user.checkPayeeName(payeeName)) {
                for (Account account : user.getAccounts()) {
                    if (account.checkNumber(number) && account.checkSortCode(sortCode)) {
                        return account;
                    }
                }
            }
        }
        Utility.printLog("No Account Found with: [Payee '%d', Account Number '%d', Sort Code '%d']", payeeName, number, sortCode);
        return null;
    }

    private boolean isValidAccountNumber(String number) {
        return number.matches("^[0-9]{8}$");
    }

    private boolean isValidSortCode(String sortCode) {
        return sortCode.matches("^[0-9]{2}-[0-9]{2}-[0-9]{2}$");
    }

    private boolean checkNumberOfAccounts(String payeeName) {
        for (User user : users) {
            if (user.checkPayeeName(payeeName)) {
                return true;
            }
        }
        return false;
    }

    private boolean notUniqueId(String id, DynamicList<?> objects) {
        for (Object object: objects) {
            if (id.equals(objects.getComparator(object))) return true;
        }
        return false;
    }

    private boolean uniqueSortCode(String sortCode) {
        for (Bank bank : banks) {
            if (bank.getSortCode().equals(sortCode)) return false;
        }
        return true;
    }

    public void displayAllUsers() {
        Utility.printTitle("ALL USERS");
        users.display(1);
    }

    public void displayAllBanks() {
        Utility.printTitle("ALL BANKS");
        banks.display(1);
    }

    public void displayBanksInformation() {
        Utility.printTitle("ALL BANK INFORMATION");
        banks.forEach(bank -> Utility.print(bank.toInfo()));
    }

    private String getNewId() {
        return UUID.randomUUID().toString();
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public boolean userLoggedIn() {
        return loggedInUser != null;
    }
}
