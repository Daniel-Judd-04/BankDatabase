import java.io.Serializable;
import java.time.LocalDateTime;

public class Account implements Serializable {
    private final String id;
    private final AccountType type;
    private final String number;
    private final String sortCode;
    private final double initialBalance;
    private double balance;
    private final DynamicList<Transaction> transactions;
    private final DynamicList<User> users;
    private final LocalDateTime createdAt;
    private Status status;

    public Account(String id, AccountType type, String number, String sortCode, double initialBalance, DynamicList<Transaction> transactions, DynamicList<User> users, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.number = number;
        this.sortCode = sortCode;
        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        this.transactions = transactions;
        this.users = users;
        this.createdAt = createdAt;
        this.status = Status.ACTIVE;
        setNames();
    }

    private void setNames() {
        transactions.setName(formatName() + " Transactions");
        users.setName(formatName() + " Users");
    }

    @Override
    public String toString() {
        if (userLoggedIn()) {
            if (type.isJoint()) { // Show names, number and balance
                return formatName() + " [" + number + "]: " + Utility.formatMoney(balance);
            } else if (status == Status.ACTIVE) { // Show type, number and balance
                return type + " [" + number + "]: " + Utility.formatMoney(balance);
            } else { // Show type, status and balance
                return type + " [" + status + "]: " + Utility.formatMoney(balance);
            }
        } // Show names, number and sort code
        // NOT balance
        return formatName() + " [" + number + "] (" + sortCode + ")";
    }

    public String toInfo() {
        StringBuilder output = new StringBuilder();

        if (userLoggedIn()) {
            boolean active = status == Status.ACTIVE;
            int LOAD_LIMIT = Main.LOAD_LIMIT;

            output.append("\n").append(formatName()).append(" Information:");
            output.append("\n > [DEBUG] UUID: ").append(Utility.formatId(id));
            output.append("\n > ").append(Utility.formatPlural("User", users.size())).append(": ").append(formatUsers());
            output.append("\n > Type: ").append(type);
            output.append("\n > Account Number: ").append(number);
            output.append("\n > Sort Code: ").append(sortCode);
            if (active) output.append("\n > Balance: ").append(Utility.formatMoney(balance));
            output.append("\n > Status: ").append(status);
            output.append("\n > Created At: ").append(Utility.formatDate(createdAt));
            if (active) {
                if (!transactions.isEmpty()) {
                    output.append("\n > Latest Transactions (" + Math.min(transactions.size(), LOAD_LIMIT) + "/" + transactions.size() + "): ");
                    for (int i = transactions.size() - 1; i >= Math.max(0, transactions.size() - LOAD_LIMIT); i--) {
                        output.append("\n").append("    > ").append(transactions.get(i).toString(this));
                    }
                } else output.append("\n > No Previous Transactions");
            }

        } else {
            Utility.printError("User of Account '" + this + "' NOT logged in!");
            output.append("No Displayable Information");
        }

        return output.toString();
    }

    private Boolean userLoggedIn() {
        return users.contains(Main.bankDatabase.getLoggedInUser());
    }

    public AccountType getType() {
        return type;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    private Boolean hasMultipleUsers() {
        return users.size() > 1;
    }

    private String formatUsers() {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < users.size(); i++) {
            output.append(users.get(i).formatName());
            if (i < users.size() - 2) output.append(", ");
            else if (i < users.size() - 1) output.append(" & ");
        }
        return output.toString();
    }

    public String formatName() {
        if (hasMultipleUsers()) {
            if (users.size() == 2 && (users.get(0).checkLastName(users.get(1)))) {
                String[] payeeParts = users.get(0).formatPayeeName().split(" ");
                return Utility.formatPossession(payeeParts[0] + " " + payeeParts[1] + " & " + users.get(1).formatPayeeName()) + " " + type + " Account";
            } else {
                return Utility.formatPossession(formatUsers()) + " " + type + " Account";
            }
            // 2 Users
            // SAME last name:
            // Mr J & Mrs E Smith's Joint Account
            // DIFF last name:
            // Mr J Smith & Mrs E Johnson's Joint Account

            // More than 2 Users
            // Mr J Smith, Mrs E Johnson & Mr L Jones's Joint Account
        }
        // Mr J Smith's Personal Account
        return Utility.formatPossession(users.get(0).formatPayeeName()) + " " + type + " Account";
    }

    public String getId() {
        return id;
    }

    public Boolean checkNumber(String attemptedNumber) {
        return number.equals(attemptedNumber);
    }

    public Boolean checkSortCode(String attemptedSortCode) {
        return sortCode.equals(attemptedSortCode);
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void changeBalance(double amount) {
        // Amount must be positive OR User must be logged in to negate money
        if (((amount < 0) && (userLoggedIn())) || (amount >= 0)) {
            balance += amount;
        } else {
            Utility.printError("ERROR: " + Utility.formatMoney(amount) + " NOT removed from Account '" + this + "'.");
        }
    }

    public Boolean canAfford(double amount) {
        return amount <= balance;
    }

    public Boolean isEmpty() {
        return balance == 0;
    }

    public Boolean checkBalance() {
        double testBalance = initialBalance;

        for (Transaction transaction : transactions) {
            testBalance += transaction.getAmount(this);
        }

        return testBalance == balance;
    }

    public double getInitialBalance(Bank bank) {
        // Verify that correct bank is given
        if (checkSortCode(bank.getSortCode())) {
            return initialBalance;
        }
        // Otherwise, return a non-possible (<0) initialBalance
        return -1;
    }
}
