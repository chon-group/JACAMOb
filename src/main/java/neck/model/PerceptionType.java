package neck.model;

public enum PerceptionType {
    INTEROCEPTION,
    PROPRIOCEPTION,
    EXTEROCEPTION;

    public String getKey() {
        return name().toLowerCase();
    }
}

