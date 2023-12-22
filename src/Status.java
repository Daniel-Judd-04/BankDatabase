public enum Status {
    ACTIVE("ACTIVE"),
    FROZEN("FROZEN"),
    CLOSED("CLOSED");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static DynamicList<Status> getAllStatuses() {
        return new DynamicList<Status>("Statuses", Status.values());
    }
}
