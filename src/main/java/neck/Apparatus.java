package neck;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import neck.model.PerceptionType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Apparatus {
    private String address = "setAddress";
    private boolean status = true;
    private String apparatusName = null;
    private final String namespace = "myBody::";

    List<Literal> interoceptions    = new ArrayList<>();
    List<Literal> exteroceptions    = new ArrayList<>();
    List<Literal> proprioceptions   = new ArrayList<>();

    public Apparatus() {}

    public Apparatus(String address) {this.address = address;}

    public String getAddress() {return address;}

    public void setAddress(String address) {this.address = address;}

    public boolean getStatus() {return status;}

    public void setStatus(boolean status) {this.status = status;}

    //public String getApparatusName() {return apparatusName;}

    public void setApparatusName(String apparatusName) {this.apparatusName = apparatusName;}

    private Literal getLiteralWithSourceBBAnnotation(Literal l, PerceptionType type) {
        return Literal.parseLiteral(this.namespace+l.toString()+"["+type.getSource()+",apparatus("+apparatusName+")]");
    }

    //

    /* TODO */
    public abstract void act(String CMD);

    public abstract JSONObject perceive();

    public abstract JSONObject embody();

    private List<Literal> getInteroceptions(){return this.interoceptions;}
    private List<Literal> getProprioceptions(){return this.proprioceptions;}
    private List<Literal> getExteroceptions(){return this.exteroceptions;}

    private void abolishProprioceptions(){this.proprioceptions.clear();}
    private void abolishInteroceptions(){this.interoceptions.clear();}
    private void abolishExteroceptions(){this.exteroceptions.clear();}

    public List<Literal> getAllPerceptions() {
        List<Literal> list = new ArrayList<>();
        list.addAll(getInteroceptions());
        list.addAll(getProprioceptions());
        list.addAll(getExteroceptions());

        abolishInteroceptions();
        abolishProprioceptions();
        abolishExteroceptions();

        return list;
    }

    public void bodyPerception() {
        JSONObject bodyResponse = perceive();
        loadPercepts(bodyResponse);
        //addPercepts();
    }

    private void addPercept(Literal l, PerceptionType type){
        switch (type) {
            case EXTEROCEPTION -> exteroceptions.add(getLiteralWithSourceBBAnnotation(l,PerceptionType.EXTEROCEPTION));
            case INTEROCEPTION -> interoceptions.add(getLiteralWithSourceBBAnnotation(l,PerceptionType.INTEROCEPTION));
            case PROPRIOCEPTION -> proprioceptions.add(getLiteralWithSourceBBAnnotation(l,PerceptionType.PROPRIOCEPTION));
        }
    }


    private void loadPercepts(JSONObject bodyResponse) {
        if (!bodyResponse.has("percepts") || bodyResponse.isNull("percepts")) return;
        JSONObject percepts = bodyResponse.getJSONObject("percepts");
        addPercepts(percepts, PerceptionType.EXTEROCEPTION);
        addPercepts(percepts, PerceptionType.INTEROCEPTION);
        addPercepts(percepts, PerceptionType.PROPRIOCEPTION);
    }

    private void addPercepts(JSONObject percepts, PerceptionType type) {
        if (!percepts.has(type.getKey())) return;

        JSONArray arr = percepts.getJSONArray(type.getKey());

        for (int i = 0; i < arr.length(); i++) {
            JSONObject p = arr.getJSONObject(i);

            String belief = p.optString("belief", null);
            if (belief == null || belief.isBlank()) continue;

            JSONArray args = p.optJSONArray("args");

            addPercept(JSON2Literal(belief, args),type);
        }
    }

    private Literal JSON2Literal(String belief, JSONArray args) {
        Literal lit = ASSyntax.createLiteral(belief);

        // sem args -> belief
        if (args == null) return lit;

        // com args -> belief(a,b,c)
        for (int i = 0; i < args.length(); i++) {
            lit.addTerm(ArgtoTerm(args.get(i)));
        }

        return lit;
    }

    private jason.asSyntax.Term ArgtoTerm(Object v) {
        if (v == null || v == JSONObject.NULL) return ASSyntax.createString("null");
        switch (v) {
            case Boolean b -> {return ASSyntax.createAtom(b ? "true" : "false");}
            case Integer i -> {return ASSyntax.createNumber(i);}
            case Long    l -> {return ASSyntax.createNumber(l);}
            case Double  d -> {return ASSyntax.createNumber(d);}
            case String  s -> {
                s = s.trim();
                if (s.isEmpty()) return ASSyntax.createString("");
                if (s.matches("[a-z][a-zA-Z0-9_]*")) {
                    return ASSyntax.createAtom(s);      // tenta virar átomo
                } else {
                    return ASSyntax.createString(String.valueOf(v));
                }
            }
            default -> {
                return null;
            }
        }
    }
}