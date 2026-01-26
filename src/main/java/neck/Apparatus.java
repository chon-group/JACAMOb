package neck;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import neck.model.PerceptionType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Apparatus {
    private String address = "setAddress";
    private boolean status = false;
    private String apparatusName = null;

    private List<Literal> interoceptions    = new ArrayList<>();
    private List<Literal> exteroceptions    = new ArrayList<>();
    private List<Literal> proprioceptions   = new ArrayList<>();

    public Apparatus() {}

    public Apparatus(String address) {this.address = address;}

    public String getAddress() {return this.address;}

    public void setAddress(String address) {this.address = address;}

    public boolean getStatus() {return this.status;}

    public void setStatus(boolean status) {this.status = status;}

    public String getApparatusName() {return this.apparatusName;}

    public void setApparatusName(String apparatusName) {this.apparatusName = apparatusName;}

    private Literal getLiteralWithSourceBBAnnotation(Literal l, PerceptionType type) {

        //adding NAMESPACE
        Literal out = Literal.parseLiteral(Body.BODY_NAMESPACE+"::"+l.toString());

        // adding source(TYPE,APPARATUS)
        out.addAnnot(
                ASSyntax.createStructure(
                        "source",
                        ASSyntax.createAtom(type.getKey()),
                        ASSyntax.createAtom(apparatusName)
                )
        );
        return out;
    }

    /* TODO in Apparatus Implementation */
    public abstract void act(String CMD);

    /* TODO in Apparatus Implementation */
    public abstract JSONObject perceive();

    /* TODO in Apparatus Implementation */
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
        loadInfo(bodyResponse);
        loadPercepts(bodyResponse);
    }

    private void loadInfo(JSONObject bdyReply){
        if (!bdyReply.has("port")) return;

        Literal litINFO = ASSyntax.createLiteral("port");
        Term portStatus     = ASSyntax.createAtom(bdyReply.getString("port"));
        Term portAddress    = ASSyntax.createString(getAddress());
        Term apparatusName;
        Term apparatusID;

        if (bdyReply.has("apparatus") && bdyReply.has("apparatusID")){
            apparatusName  = neck.util.Util.stringToAtom(bdyReply.getString("apparatus"));
            apparatusID    = ASSyntax.createNumber(bdyReply.getLong("apparatusID"));
        }else{
            apparatusName  = neck.util.Util.stringToAtom("unknown");
            apparatusID    = ASSyntax.createNumber(0);
        }


        litINFO.addTerm(portStatus);
        litINFO.addTerm(portAddress);
        litINFO.addTerm(apparatusName);
        litINFO.addTerm(apparatusID);

        addPercept(litINFO,PerceptionType.INTEROCEPTION);
    }

    private void loadPercepts(JSONObject bodyResponse) {
        if (!bodyResponse.has("percepts") || bodyResponse.isNull("percepts")) return;
        JSONObject percepts = bodyResponse.getJSONObject("percepts");
        addPerceptsByPerceptionsType(percepts, PerceptionType.EXTEROCEPTION);
        addPerceptsByPerceptionsType(percepts, PerceptionType.INTEROCEPTION);
        addPerceptsByPerceptionsType(percepts, PerceptionType.PROPRIOCEPTION);
    }

    private void addPerceptsByPerceptionsType(JSONObject perceptions, PerceptionType perceptionType) {
        /* EXPECTED...
        {
            "exteroception" :[{"belief":"b1","args":[0,1,2]},{"belief":"b2","args":[0,1,2]},{"belief":"bn"}],
            "interoception" :[{"belief":"b1","args":[0,1,2]},{"belief":"b2","args":[0,1,2]},{"belief":"bn"}],
            "proprioception":[{"belief":"b1","args":[0,1,2]},{"belief":"b2","args":[0,1,2]},{"belief":"bn"}]
        }
        */
        if (!perceptions.has(perceptionType.getKey())) return;

        /* Extracting the array of the percepts type (based on PerceptionType informed)
            "exteroception" : [{"belief":"b1","args":[0,1,2]},{"belief":"b2","args":[0,1,2]},{"belief":"bn"}]
        */
        JSONArray filteredPerceptionsByType = perceptions.getJSONArray(perceptionType.getKey());

        /*  Traversing the chosen array
            [
                {"belief":"b1","args":[0,1,2]},
                {"belief":"b2","args":[0,1,2]},
                {"belief":"bn"}
            ]
        */
        for (int i = 0; i < filteredPerceptionsByType.length(); i++) {
            /* getting the object (i)
            *   {"belief":"b1","args":[0,1,2]}
            * */
            JSONObject jsonObject = filteredPerceptionsByType.getJSONObject(i);

            /* EXPECTED
            * {"belief":"b1","args":[0,1,2]}
            * */
            if(jsonObject.has("belief")){
                /* getting the belief name -->  "belief":"b1"   */
                Literal belief = neck.util.Util.JSONObjectToLiteral(jsonObject,"belief");

                /* getting the args array -->   "args":[0,1,2]  */
                if(jsonObject.has("args")){
                    JSONArray termsArgs = jsonObject.getJSONArray("args");
                    belief = neck.util.Util.addJSONArrayAsTermsInLiteral(belief,termsArgs);
                }
                addPercept(belief,perceptionType);
            }
        }
    }

    private void addPercept(Literal l, PerceptionType type){
        switch (type) {
            case EXTEROCEPTION -> exteroceptions.add(getLiteralWithSourceBBAnnotation(l,PerceptionType.EXTEROCEPTION));
            case INTEROCEPTION -> interoceptions.add(getLiteralWithSourceBBAnnotation(l,PerceptionType.INTEROCEPTION));
            case PROPRIOCEPTION -> proprioceptions.add(getLiteralWithSourceBBAnnotation(l,PerceptionType.PROPRIOCEPTION));
        }
    }
}