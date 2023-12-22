import java.io.Serializable;

public class Bank implements Serializable {
    private final String id;
    private final String name;
    private final String sortCode;
    private final double interestRate;
    private final double transactionCharge;
    private final double initialBalance;
    private double balance;
    private final DynamicList<Account> accounts;
    private final DynamicList<Transaction> transactions;

    public Bank(String id, String name, String sortCode, double interestRate, double transactionCharge, double initialBalance, DynamicList<Account> accounts, DynamicList<Transaction> transactions) {
        this.id = id;
        this.name = name; // A Bank Name
        this.sortCode = sortCode; // XX-XX-XX
        this.interestRate = interestRate; // 0.01
        this.transactionCharge = transactionCharge; // 0.01
        this.initialBalance = initialBalance;
        this.balance = initialBalance;
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public String getId() {
        return id;
    }

    public String toString() {
        return name + " [" + sortCode + "] (" + Utility.formatMoney(balance) + "): Interest Rate = " + Utility.formatPercent(interestRate) + " | Transaction Charge = " + Utility.formatPercent(transactionCharge);
    }

    public String toInfo() {
        int LOAD_LIMIT = Main.LOAD_LIMIT;
        StringBuilder output = new StringBuilder();

        output.append("\n").append(name).append(" Information:");
        output.append("\n > Sort Code: ").append(sortCode);
        output.append("\n > Interest Rate: ").append(Utility.formatPercent(interestRate));
        output.append("\n > Transaction Charge: ").append(Utility.formatPercent(transactionCharge));
        output.append("\n > Balance: ").append(Utility.formatMoney(balance));
        if (!transactions.isEmpty()) {
            output.append("\n > Latest Transactions (" + Math.min(transactions.size(), LOAD_LIMIT) + "/" + transactions.size() + "): ");
            for (int i = transactions.size() - 1; i >= Math.max(0, transactions.size() - LOAD_LIMIT); i--) {
                output.append("\n").append("    > ").append(transactions.get(i).toString());
            }
        } else output.append("\n > No Previous Transactions");

        return output.toString();
    }

    public String getSortCode() {
        return sortCode;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public double getTransactionCharge() {
        return transactionCharge;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void changeMoney(double amount) {
        balance += amount;
    }

    public Boolean canAfford(double amount) {
        return amount <= balance;
    }

    public Boolean checkBalance() {
        double testBalance = initialBalance;

        for (Account account : accounts) {
            testBalance -= account.getInitialBalance(this);
        }

        for (Transaction transaction : transactions) {
            testBalance += transaction.getCharge();
        }

        return testBalance == balance;
    }
}
