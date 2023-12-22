import java.io.Serializable;

public class Connection implements Serializable {
    private final String id;
    private final User user;
    private final Account toAccount;
    private final String reference;

    public Connection(String id, User user, Account toAccount, String reference) {
        this.id = id;
        this.user = user;
        this.toAccount = toAccount;
        this.reference = reference;
    }

    public Connection(String id, User user, Account toAccount) {
        this.id = id;
        this.user = user;
        this.toAccount = toAccount;
        this.reference = "No Reference";
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return toAccount.toString();
    }

    public Account getToAccount() {
        return toAccount;
    }

    public String getReference() {
        return reference;
    }
}
