import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;

public class User implements Serializable {
    private final String id;
    private final String username;
    private final Password password;
    private final DynamicList<SecurityQuestion> securityQuestions;
    private final String title;
    private final String fullName;
    private final DynamicList<Account> accounts;
    private final DynamicList<Connection> connectedAccounts;
    private LocalDateTime unlockTime;
    private int numberOfTimesLocked;

    public User(String id, String username, Password password, DynamicList<SecurityQuestion> securityQuestions, String title, String fullName, DynamicList<Account> accounts, DynamicList<Connection> connectedAccounts, LocalDateTime unlockTime, int numberOfTimesLocked) {
        this.id = id; // UUID
        this.username = username; // J_Smith
        this.password = password; // Password obj
        this.securityQuestions = securityQuestions;

        this.title = title; // Mr
        this.fullName = fullName; // John Smith

        this.accounts = accounts; // User accounts
        this.connectedAccounts = connectedAccounts; // External Connected Accounts

        this.unlockTime = unlockTime; // LocalDateTime where the account is unlocked
        this.numberOfTimesLocked = numberOfTimesLocked; // Number of times locked since last login

        setNames(); // Set correctly formatted DynamicList names
    }

    private void setNames() {
        securityQuestions.setName(Utility.formatPossession(formatName()) + " Security Questions");
        accounts.setName(Utility.formatPossession(formatName()) + " Accounts");
        connectedAccounts.setName(Utility.formatPossession(formatName()) + " Connected Accounts");
    }

    @Override
    public String toString() {
        if (isUnlocked()) return formatName() + " (" + accounts.size() + " " + Utility.formatPlural("Account", accounts.size()) + ")";
        return formatName() + " [LOCKED for " + Utility.formatDuration(Utility.duration(Utility.now(), unlockTime)) + "]";
    }

    public String toInfo() {
        StringBuilder output = new StringBuilder();

        output.append("\n").append(formatName()).append(" Information:");
        output.append("\n - Username: ").append(username);
        output.append("\n - Title: ").append(title);
        output.append("\n - Full Name: ").append(fullName);
        if (!accounts.isEmpty()) {
            output.append("\n - Accounts: ");
            for (int i = 0; i < accounts.size(); i++) {
                output.append("\n").append("    - ").append(accounts.get(i));
            }
        } else output.append("\n - No Accounts");
        if (!connectedAccounts.isEmpty()) {
            output.append("\n - Connected Accounts: ");
            for (int i = 0; i < connectedAccounts.size(); i++) {
                output.append("\n").append("    - ").append(connectedAccounts.get(i));
            }
        } else output.append("\n - No Connected Accounts");

        return output.toString();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    // Mr John A. Smith
    public String formatName() {
        return title + " " + firstName() + " " + middleNames() + lastName();
    }

    // 'MR J SMITH'
    public String formatPayeeName() {
        return title + " " + firstName().charAt(0) + " " + lastName();
    }

    public Boolean checkPayeeName(String payeeName) {
        return payeeName.equalsIgnoreCase(formatPayeeName());
    }

    public Boolean checkLastName(User user) {
        return lastName().equals(user.lastName());
    }

    // 'John'
    private String firstName() {
        return fullName.split(" ")[0];
    }

    // 'A. B. C.'
    private String middleNames() {
        StringBuilder output = new StringBuilder();
        String[] names = fullName.split(" ");

        for (int i = 1; i < names.length - 1; i++) {
            output.append(names[i].charAt(0)).append(". ");
        }

        return output.toString();
    }

    // Smith
    private String lastName() {
        return fullName.split(" ")[fullName.split(" ").length - 1];
    }

    public Boolean checkFullName(String attemptedFullName) {
        return fullName.equals(attemptedFullName);
    }

    public DynamicList<Account> getAccounts() {
        return accounts;
    }

    public Boolean accountTypeExists(String type) {
        for (Account account : accounts) {
            if (account.getType().toString().equals(type) && !account.getStatus().equals(Status.CLOSED)) return true;
        }
        return false;
    }

    public DynamicList<Connection> getConnectedAccounts() {
        return connectedAccounts;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public void addConnection(Connection connection) {
        connectedAccounts.add(connection);
    }

    public Boolean checkPassword(char[] attemptedPassword) {
        Boolean valid = password.verifyPassword(attemptedPassword);
        Arrays.fill(attemptedPassword, '\0');
        return valid;
    }

    public SecurityQuestion getSecurityQuestion() {
        return securityQuestions.get(Utility.randomIndex(securityQuestions));
    }

    public boolean checkAttempts() {
        return password.checkAttempts();
    }

    public Boolean checkASecurityQuestion() {
        boolean valid = false;
        SecurityQuestion securityQuestion = getSecurityQuestion();
        char[] userAnswer;
        do {
            userAnswer = Utility.getPassword("[SQ] Enter " + securityQuestion.toString());
            if (userAnswer.length != 0) {
                if (securityQuestion.checkAttempts()) {
                    if (securityQuestion.checkRegex(new String(userAnswer))) {
                        if (securityQuestion.checkAnswer(userAnswer)) {
                            valid = true;
                        } else Utility.printLog("Answer Incorrect!");
                    } else Utility.printLog("Answer Incorrectly Formatted!");
                } else {
                    Utility.printError("Security Question Incorrectly Answered Too Many Times!");
                    break;
                }
            } else break;
        } while (!valid);
        Arrays.fill(userAnswer, '\0');
        return valid;
    }

    public Boolean hasAccounts() {
        return !accounts.isEmpty();
    }

    public void resetPasswordAttempts() {
        password.resetAttempts();
    }

    public void resetNumberOfTimesLocked() {
        numberOfTimesLocked = 0;
    }

    public boolean isUnlocked() {
        return unlockTime.isBefore(Utility.now());
    }

    public void lockUser() {
        numberOfTimesLocked++;
        long time = (long) Math.pow(numberOfTimesLocked, 2);
        // In - 1, 4, 9, 16, 25, 36, 49, 64, ...
        // Cu - 1, 5, 14, 30, 55, 91, 140, 204, ...
        unlockTime = Utility.now().plusMinutes(time);

        Utility.printLog( "%d has been locked for %d minutes!", this, time);
    }
}
