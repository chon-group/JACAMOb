package neck.util;

import com.fazecast.jSerialComm.SerialPort;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.logging.Logger;

import static neck.util.CompilerLite.logger;

public class Util {
    private static Logger logger = Logger.getLogger("NECK");

    public static Atom stringToAtom(String input) {
        if (input == null || input.isBlank()) {
            return ASSyntax.createAtom("v");
        }

        String s = input.trim();

        // 1) remove acentos (sem mexer no case)
        s = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 2) remove caracteres inválidos (mantém maiúsculas)
        s = s.replaceAll("[^a-zA-Z0-9_]", "_");

        // 3) remove underscores repetidos
        s = s.replaceAll("_+", "_");

        // 4) garante primeira letra minúscula
        char first = s.charAt(0);
        if (!Character.isLowerCase(first)) {
            if (Character.isLetter(first)) {
                s = Character.toLowerCase(first) + s.substring(1);
            } else {
                s = "v_" + s;
            }
        }

        // 5) fallback final de segurança
        if (!s.matches("[a-z][a-zA-Z0-9_]*")) {
            s = "v";
        }

        return ASSyntax.createAtom(s);
    }

    public static Literal JSONObjectToLiteral(JSONObject json, String key){
        return ASSyntax.createLiteral(json.getString(key));
    }

    public static Literal addJSONArrayAsTermsInLiteral(Literal literal, JSONArray args) {

        /* EXPECTED
          "args":[0,1,2]
        */
        if (args != null){
            for (int i = 0; i < args.length(); i++) {
                literal.addTerm(jsonObjectToTerm(args.get(i)));
            }
        }
        return literal;
    }

     public static String getFormatedPortName(String in){
        if (in != null){
            in = in.trim();
            // remove aspas apenas se estiverem no início e no fim
            if (in.length() >= 2 &&
                    in.startsWith("\"") &&
                    in.endsWith("\"")) {
                in = in.substring(1, in.length() - 1);
            }
        }
        return in;
    }

    public static boolean serialPortIsAvailable(String portAddress) {
        try {
            SerialPort p = SerialPort.getCommPort(getFormatedPortName(portAddress));
            if (p.openPort(0)){
                p.closePort();
                p=null;
                return true;
            }else{
                p=null;
                return false;
            }
        }catch (Exception ex){
            logger.fine(ex.toString());
            return false;
        }
    }

    /* PRIVATES */
    private static Term jsonObjectToTerm(Object object) {

        if(object != null && object != JSONObject.NULL){
            switch (object) {
                case Boolean b -> {return ASSyntax.createAtom(b ? "true" : "false");}
                case Integer i -> {return ASSyntax.createNumber(i);}
                case Long    l -> {return ASSyntax.createNumber(l);}
                case Double  d -> {return ASSyntax.createNumber(d);}
                case String  s -> {
                    s = s.trim();
                    if (!s.isEmpty() && s.matches("[a-z][a-zA-Z0-9_]*")) {
                        return ASSyntax.createAtom(s);                          // if a valid Atom
                    } else {
                        return ASSyntax.createString(String.valueOf(object));   // if a not valid Atom
                    }
                }
                default -> {
                    return null;
                }
            }
        }
        return null;
    }
}
