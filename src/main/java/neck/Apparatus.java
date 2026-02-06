package neck;

import jason.asSyntax.*;
import jason.asSyntax.parser.ParseException;
import jason.pl.PlanLibrary;
import neck.model.BodyResponse;
import neck.model.PerceptionType;
import neck.model.SerialPortStatus;
import neck.util.SerialComm;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Apparatus {
    //private String address = "setAddress";
    //private boolean status = false;
    private String  apparatusName = null;
    private String  hwAppName = null;
    private Set<String> supportedActions = new HashSet<>();
    private Long    hwAppID;
    private SerialComm serialComm = null;


    private List<Plan>    apparatusPlans    = new ArrayList<>();
    private List<Literal> interoceptions    = new ArrayList<>();
    private List<Literal> exteroceptions    = new ArrayList<>();
    private List<Literal> proprioceptions   = new ArrayList<>();
    private List<Literal> desires           = new ArrayList<>();

    public Apparatus() {}

    public Apparatus(SerialComm serial){
        this.serialComm = serial;
        connect();
        if(getStatus()) loadApparatusInfo();
    }
    //public Apparatus(String address) {setAddress(address);}
    private void connect(){
        if(this.serialComm != null && this.serialComm.getPortStatus() == SerialPortStatus.ON) this.serialComm.closeConnection();
        this.serialComm.openConnection();
    }
    public String getAddress() {
        if(this.serialComm != null) return this.serialComm.getPortAddress();
        return null;
    }

    public boolean supportsAction(String actionName){
        return this.supportedActions.contains(actionName);
    }

    private void addPlan(String trigger, String context, String body) throws ParseException {
        if (trigger == null || trigger.isBlank()) throw new IllegalArgumentException("Trigger cannot be null");
        if (context == null || context.isBlank()) context = "true";
        if (body == null || body.isBlank()) body = "true";

        Plan p = ASSyntax.parsePlan("+!"+trigger + " : " + context + " <- " + body + ".");

        if (p == null) return;
        this.apparatusPlans.add(p);
    }

    public SerialComm getSerialComm(){
        return this.serialComm;
    }
    public void disconnect(){
        this.serialComm.closeConnection();
    }

    public boolean getStatus() {
        if(this.serialComm.getPortStatus() == SerialPortStatus.ON) return true;
        else return false;
    }

    public SerialPortStatus getConnectionStatus(){
        return this.serialComm.getPortStatus();
    }

    public String getHwAppName(){return this.hwAppName;}

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
    public abstract BodyResponse act(Term actionTerm);

    /* TODO in Apparatus Implementation */
    public abstract JSONObject perceive();

    /* TODO in Apparatus Implementation */
    public abstract JSONObject embody();

    private List<Literal> getInteroceptions(){return this.interoceptions;}
    private List<Literal> getProprioceptions(){return this.proprioceptions;}
    private List<Literal> getExteroceptions(){return this.exteroceptions;}

    public List<Literal> getDesires(){
        return  this.desires;
    }

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
        loadConnectionInfo(bodyResponse);
        loadPercepts(bodyResponse);
        loadDesires(bodyResponse);
    }

    private void loadConnectionInfo(JSONObject bdyReply){
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

    private void loadDesires(JSONObject bodyResponse){
        this.desires.clear();
        if (!bodyResponse.has("desires") || bodyResponse.isNull("desires")) return;

        JSONArray desires = bodyResponse.getJSONArray("desires");
        for (int i=0; i<desires.length(); i++){
            JSONObject desire = desires.getJSONObject(i);
            Literal newDesire;
            if(desire.has("desire") && desire.has("args")){
                JSONArray argsDesire = desire.getJSONArray("args");
                newDesire = neck.util.Util.JSONObjectToLiteral(desire,"desire");
                this.desires.add(neck.util.Util.addJSONArrayAsTermsInLiteral(newDesire,argsDesire));
            }
            else if(desire.has("desire") && !desire.has("args")) {
                this.desires.add(neck.util.Util.JSONObjectToLiteral(desire,"desire"));
            }
        }
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
            "exteroception" :[{"percept":"b1","args":[0,1,2]},{"percept":"b2","args":[0,1,2]},{"percept":"bn"}],
            "interoception" :[{"percept":"b1","args":[0,1,2]},{"percept":"b2","args":[0,1,2]},{"percept":"bn"}],
            "proprioception":[{"percept":"b1","args":[0,1,2]},{"percept":"b2","args":[0,1,2]},{"percept":"bn"}]
        }
        */
        if (!perceptions.has(perceptionType.getKey())) return;

        /* Extracting the array of the percepts type (based on PerceptionType informed)
            "exteroception" : [{"percept":"b1","args":[0,1,2]},{"percept":"b2","args":[0,1,2]},{"percept":"bn"}]
        */
        JSONArray filteredPerceptionsByType = perceptions.getJSONArray(perceptionType.getKey());

        /*  Traversing the chosen array
            [
                {"percept":"b1","args":[0,1,2]},
                {"percept":"b2","args":[0,1,2]},
                {"percept":"bn"}
            ]
        */
        for (int i = 0; i < filteredPerceptionsByType.length(); i++) {
            /* getting the object (i)
            *   {"percept":"b1","args":[0,1,2]}
            * */
            JSONObject jsonObject = filteredPerceptionsByType.getJSONObject(i);

            /* EXPECTED
            * {"percept":"b1","args":[0,1,2]}
            * */
            if(jsonObject.has("percept")){
                /* getting the belief name -->  "percept":"b1"   */
                Literal belief = neck.util.Util.JSONObjectToLiteral(jsonObject,"percept");

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

    private void loadApparatusInfo() {
        JSONObject jsonObject = this.serialComm.sendMsg("getActions");

        this.hwAppName = jsonObject.optString("apparatus", "unknown");
        this.hwAppID   = jsonObject.optLong("apparatusID", 0L);

        this.supportedActions.clear();

        if (jsonObject.has("actions")) {
            JSONArray actions = jsonObject.getJSONArray("actions");

            for (int i = 0; i < actions.length(); i++) {
                JSONObject actionObj = actions.getJSONObject(i);

                if (actionObj.has("actionName")) {
                    this.supportedActions.add(actionObj.getString("actionName"));
                }
            }
        }
    }

    private void loadPlans(){
        JSONObject jsonObject = this.serialComm.sendMsg("getSkills");
        if (jsonObject.has("skills")) {

            JSONArray skills = jsonObject.getJSONArray("skills");

            for (int i = 0; i < skills.length(); i++) {
                JSONObject skillObj = skills.getJSONObject(i);
                String context = null;
                String trigger = null;
                String planBody = null;
                if(skillObj.has("context") && skillObj.has("skill") && skillObj.has("plans")){
                    trigger     = skillObj.get("skill").toString();
                    context     = skillObj.get("context").toString();
                    planBody    = skillObj.get("plans").toString();

                    if (context.equals("FILE")){
                        //loadFile...
                        System.out.println("é FILE skipping");
                    }
                    else if (context.equals("URL")){
                        //download
                        System.out.println("é URL skipping ");
                    }
                    else{
                        try {
                            addPlan(trigger,context,planBody);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        //carrega na mente
                        // System.out.println("é outro "+context);
                    }
                }
            }
        }
    }

    public void loadPlansFromDevice(){
        this.apparatusPlans.clear();
        loadPlans();
    }

    public Plan[] getPlans(){
        if(this.apparatusPlans.isEmpty()) return null;

        Plan[] planList = new Plan[this.apparatusPlans.size()];
        for(int i=0; i<this.apparatusPlans.size(); i++){
            planList[i] = this.apparatusPlans.get(i);
        }
        return planList;
    }
}