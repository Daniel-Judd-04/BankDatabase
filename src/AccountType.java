public enum AccountType {
    SAVINGS("Savings", 1.05, false),
    YOUNG_SAVERS("Young Savers", 1.10, false),
    SPENDING("Spending", 1.01, false),
    CHECKING("Checking", 1.01, false),
    BUSINESS("Business", 1.01, true),
    JOINT("Joint", 1.04, true),
    PERSONAL("Personal", 1.05, false),
    STUDENT("Student", 1.08, false);

    private final String value;
    private final double interestRate;
    private final Boolean joint;

    AccountType(String value, double interestRate, Boolean joint) {
        this.value = value;
        this.interestRate = interestRate;
        this.joint = joint;
    }

    public String toString() {
        return value;
    }

    public static DynamicList<AccountType> getAllAccountTypes() {
        return new DynamicList<>("Account Types", AccountType.values());
    }

    public Boolean isJoint() {
        return joint;
    }
}
