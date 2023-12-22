import java.io.Serializable;
import java.time.LocalDateTime;

public class Transaction implements Serializable {
    private final String id;
    private final Account fromAccount;
    private final Account toAccount;
    private final double amount;
    private final double charge;
    private final LocalDateTime timeStamp;
    private final String reference;

    public Transaction(String id, Account fromAccount, Account toAccount, double amount, double charge, LocalDateTime timeStamp, String reference) {
        this.id = id;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.charge = charge;
        this.timeStamp = timeStamp;
        this.reference = reference;
    }

    public Transaction(String id, Account fromAccount, Account toAccount, double amount, double charge, LocalDateTime timeStamp) {
        this.id = id;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.charge = charge;
        this.timeStamp = timeStamp;
        this.reference = "No Reference";
    }

    public String toString(Account account) {
        // £556.23 FROM/TO Another Account Name (This is a reference) [@123143]
        String endingInfo = " (" + reference + ") [@" + Utility.formatDate(timeStamp) + "]";
        if (account != null) {
            if (account == fromAccount) return Utility.formatMoney(amount) + " (+" + Utility.formatMoney(charge) + " CHARGE) TO " + toAccount.formatName() + endingInfo;
            return Utility.formatMoney(amount) + " FROM " + fromAccount.formatName() + endingInfo;
        }
        return Utility.formatMoney(amount) + " (+" + Utility.formatMoney(charge) + " CHARGE) FROM " + fromAccount.formatName() + " TO " + toAccount.formatName() + endingInfo;
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public double getAmount(Account account) {
        if (account == fromAccount) return -1 * (amount + charge);
        return amount;
    }

    public double getCharge() {
        return charge;
    }

    public String getId() {
        return id;
    }
}
