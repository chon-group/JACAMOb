package neck.model;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;
import org.json.JSONObject;

public enum BodyResponse {
    EXECUTED,       // The action was successfully executed and caused a state change in the body.
    UNABLE,         // The action was understood and attempted, but could not be completed due to physical, contextual, or operational constraints.
    ALREADY,        // The action was valid, but the body was already in the requested state, therefore no state change was necessary.
    REJECTED,       // The action was understood, but deliberately refused by the body before execution (e.g., safety rules, internal policies).
    INVALID,        // The action request was malformed or semantically invalid.
    UNKNOWN,        // The requested action does not exist or is not recognized at the core level.
    PERCEPTED,      // The perception was successfully obtained and is considered valid.
    UNAVAILABLE,    // The perception could not be obtained (e.g., sensor failure).
    UNCHANGED;      // The perception was successfully obtained, but its value is the same as in the previous perception cycle.

    public static BodyResponse stringToBodyResponse(String bodyResponse) {
        if (bodyResponse == null) return null;
        try {
            return BodyResponse.valueOf(bodyResponse.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static BodyResponse jsonObjectToBodyResponse(JSONObject jsonObject){
        if(jsonObject.has("bodyResponse")){
            return stringToBodyResponse(jsonObject.get("bodyResponse").toString());
        }
        return null;
    }

    public Term toTerm() {
        return ASSyntax.createAtom(name().toLowerCase());
    }
}
