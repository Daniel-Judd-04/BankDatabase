public class Main {
    public static BankDatabase bankDatabase = new BankDatabase();

    public static int LOAD_LIMIT = 5;

    public static void main(String[] args) {
        bankDatabase = Utility.readAndDecryptObject("550e8400-e29b-41d4-a716-48465144r0d7");

        if (bankDatabase != null) {
            bankDatabase.updateSecurityQuestions();
            while (displayOptions()) {
                Utility.encryptAndWriteObject(bankDatabase, "550e8400-e29b-41d4-a716-48465144r0d7");
            }
        } else {
            Utility.printError("Error reading and decrypting bankDatabase");
        }
    }

    public static Boolean displayOptions() {
        DynamicList<String> options;
        boolean admin = !bankDatabase.userLoggedIn();

        if (!admin) options = new DynamicList<>(Utility.formatPossession(bankDatabase.getLoggedInUser().formatName()) + " Options");
        else options = new DynamicList<>("Admin Options");

        if (admin) {
            options.add("Add User");
            options.add("Add Bank");
        }
        if (bankDatabase.getNumberOfUsers() > 0 && bankDatabase.getNumberOfBanks() > 0) {
            if (admin) {
                options.add("View Users");
                options.add("View Banks");
                options.add("View Detailed Banks");
                options.add("Remove User");
                options.add("User Login");
            } else {
                options.add("View User Info");
                options.add("Add Account");
                if (bankDatabase.getLoggedInUser().hasAccounts()) {
                    options.add("View Account Info");
                    options.add("Change Account Status");
                    options.add("Make Internal Transaction Between Your Accounts");
                    if (bankDatabase.getNumberOfUsers() > 1) options.add("Make External Transaction");
                }
                options.add("Logout");
            }
        }

        options.add("EXIT");

        String choice = options.get(Utility.getIntInput("Enter choice", options));

        switch (choice) {
            case "Add User":
                bankDatabase.addUser();
                break;
            case "Add Bank":
                bankDatabase.addBank();
                break;
            case "View Users":
                bankDatabase.displayAllUsers();
                break;
            case "View Banks":
                bankDatabase.displayAllBanks();
                break;
            case "View Detailed Banks":
                bankDatabase.displayBanksInformation();
                break;
            case "Remove User":
                bankDatabase.removeUser();
                break;
            case "User Login":
                bankDatabase.login();
                break;
            case "View User Info":
                bankDatabase.viewUser();
                break;
            case "View Account Info":
                bankDatabase.viewAccounts();
                break;
            case "Add Account":
                bankDatabase.addAccount();
                break;
            case "Change Account Status":
                bankDatabase.changeAccountStatus();
                break;
            case "Make Internal Transaction Between Your Accounts":
                bankDatabase.makeInternalTransaction();
                break;
            case "Make External Transaction":
                bankDatabase.makeExternalTransaction();
                break;
            case "Logout":
                bankDatabase.logout();
                break;
            case "EXIT":
                // Break Loop
                return false;
        }

        // Loop
        return true;
    }
}