package neck.util;

import com.fazecast.jSerialComm.SerialPort;
import jason.NoValueException;
import jason.asSyntax.*;
import neck.Apparatus;
import neck.DefaultApparatus;
import neck.model.BodyResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public static String[] getAvailableSerialPorts(){
        SerialPort[] ports = SerialPort.getCommPorts();
        if (ports.length == 0) return null;

        List<String> portsList = new ArrayList<>();
        for (SerialPort port : ports) {
            if(serialPortIsAvailable(port.getSystemPortPath()))
                portsList.add(port.getSystemPortPath());
        }

        ports = null;
        Collections.sort(portsList);
        String[] out = new String[portsList.size()];

        for (int i = 0; i < portsList.size(); i++)
            out[i] = portsList.get(i);

        return out;
    }

    public static boolean serialPortIsAvailable(String portAddress) {
        if(portAddress == null) return false;

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

    public static Term getPortAddressByApparatusName(Term apparatusName){
        logger.fine("Searching for Apparatus "+apparatusName.toString());
        String[] availablePorts = getAvailableSerialPorts();
        for(int i=0; i<availablePorts.length; i++){
            Apparatus tempApparatus = null;
            tempApparatus = new DefaultApparatus(availablePorts[i]);
            if (tempApparatus.getStatus() && apparatusName.toString().equals(tempApparatus.getHwAppName())){
                tempApparatus.disconnect();
                logger.info("Apparatus "+apparatusName.toString()+" found at "+availablePorts[i]);
                return ASSyntax.createString(availablePorts[i]);
            }
            tempApparatus.disconnect();
        }
        logger.severe("Apparatus "+apparatusName.toString()+" not found!");
        return null;
    }
    public static String getFunctor(Term term) {
        if (term == null) return null;

        if (term.isStructure()) return ((Structure) term).getFunctor();
        if (term.isAtom())      return ((Atom) term).getFunctor();

        return null;
    }

    public static Object[] argsOfTermToObjects(Term actionTerm) {
        if (actionTerm.isAtom()) return null;

        if (!actionTerm.isLiteral()) return null;
        Literal literal = ((Literal) actionTerm);

        Object[] objects = new Object[literal.getArity()];

        for (int i=0; i<literal.getArity(); i++){
            Term asTerm = literal.getTerm(i);
                try{
                    objects[i] = Integer.parseInt(asTerm.toString());
                    continue;
                }catch (NumberFormatException ignore){}

                try{
                    objects[i] = Long.parseLong(asTerm.toString());
                    continue;
                }catch (NumberFormatException ignore){}

                try{
                    objects[i] = Double.parseDouble(asTerm.toString());
                    continue;
                }catch (NumberFormatException ignore){}

                try {
                    objects[i] = Boolean.parseBoolean(asTerm.toString());
                    continue;
                }catch (Exception ignore){}

                try {
                    objects[i] = asTerm.toString();
                    continue;
                }catch (Exception ignore){}
        }
        return objects;
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
