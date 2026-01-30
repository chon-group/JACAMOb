package neck.model;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;
import org.json.JSONObject;

public enum BodyResponse {
    EXECUTED,   // action caused a state change
    FAILED,     // action was attempted but could not be completed
    UNCHANGED,  // action was valid but no change was needed
    REJECTED,   // action was understood but refused before execution
    INVALID,    // malformed or semantically invalid action
    UNKNOWN;    // action does not exist (core-level)

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
