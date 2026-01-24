package neck.model;

public enum BodyResponse {
    EXECUTED,   // action caused a state change
    FAILED,     // action was attempted but could not be completed
    UNCHANGED,  // action was valid but no change was needed
    REJECTED,   // action was understood but refused before execution
    INVALID,    // malformed or semantically invalid action
    UNKNOWN    // action does not exist (core-level)
}
