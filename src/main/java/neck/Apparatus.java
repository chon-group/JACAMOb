package neck;

import jason.asSyntax.*;
import neck.model.BodyResponse;
import neck.model.PerceptionType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public abstract class Apparatus {

    private String apparatusName = null;
    private String hwAppName = null;
    private Long hwAppID = null;

    private final Set<String> supportedActions = new HashSet<>();

    private final Logger logger;

    private final List<Plan> apparatusPlans = new ArrayList<>();
    private final List<Literal> interoceptions = new ArrayList<>();
    private final List<Literal> exteroceptions = new ArrayList<>();
    private final List<Literal> proprioceptions = new ArrayList<>();
    private final List<Literal> desires = new ArrayList<>();


    protected Apparatus() {
        this.logger = Logger.getLogger("NECK");
    }


    /**
     * Deve ser chamado pela implementação concreta depois que os
     * recursos necessários à comunicação física forem inicializados.
     */
    protected final void init() {
        attach();

        if (getStatus()) {
            loadBodyActions();
        }
    }


    /* ============================================================
       PHYSICAL INTERFACE
       ============================================================ */

    /**
     * Estabelece a comunicação com o recurso físico.
     */
    public abstract void attach();


    /**
     * Encerra a comunicação com o recurso físico.
     */
    public abstract void detach();


    /**
     * Informa se o recurso físico está disponível.
     */
    public abstract boolean getStatus();


    /**
     * Retorna uma descrição do estado atual da comunicação.
     */
    public abstract String getConnectionStatus();


    /**
     * Retorna o endereço ou identificação física utilizada
     * pela implementação concreta.
     */
    public abstract String getAddress();


    /**
     * Obtém a descrição das ações disponibilizadas pelo apparatus.
     */
    protected abstract JSONObject getActions();


    /**
     * Obtém o know-how disponibilizado pelo apparatus.
     */
    protected abstract JSONObject getKnowHow();


    /**
     * Obtém as percepções produzidas pelo apparatus.
     */
    public abstract JSONObject perceive();


    /**
     * Executa uma ação no mundo físico.
     */
    public abstract BodyResponse act(Term actionTerm);


    /**
     * Executa operações específicas necessárias à incorporação.
     */
    public abstract JSONObject embody();


    /* ============================================================
       APPARATUS IDENTIFICATION
       ============================================================ */

    public String getHwAppName() {
        return hwAppName;
    }


    public String getApparatusName() {
        return apparatusName;
    }


    public void setApparatusName(String apparatusName) {
        this.apparatusName = apparatusName;
    }


    /* ============================================================
       ACTION
       ============================================================ */

    public boolean supportsAction(String actionName) {
        return supportedActions.contains(actionName);
    }


    private void loadBodyActions() {
        JSONObject jsonObject = getActions();

        if (jsonObject == null) {
            return;
        }

        this.hwAppName = jsonObject.optString("apparatus", "unknown");
        this.hwAppID = jsonObject.optLong("apparatusID", 0L);

        logger.fine(
                "hwApparatusName: " + this.hwAppName
                        + " hwApparatusID: " + this.hwAppID
        );

        supportedActions.clear();

        if (!jsonObject.has("actions")) {
            return;
        }

        JSONArray actions = jsonObject.getJSONArray("actions");

        for (int i = 0; i < actions.length(); i++) {
            JSONObject actionObj = actions.getJSONObject(i);

            if (actionObj.has("action")) {
                String action = actionObj.getString("action");

                supportedActions.add(action);

                logger.fine("supportedAction: " + action);
            }
        }
    }


    /* ============================================================
       PERCEPTION
       ============================================================ */

    public void sense() {
        JSONObject bodyResponse = perceive();

        if (bodyResponse == null) {
            return;
        }

        loadConnectionInfo(bodyResponse);
        loadPercepts(bodyResponse);
        loadDesires(bodyResponse);
    }


    private Literal getLiteralWithSourceBBAnnotation(
            Literal literal,
            PerceptionType type
    ) {

        Literal out = Literal.parseLiteral(
                Body.BODY_NAMESPACE + "::" + literal
        );

        out.addAnnot(
                ASSyntax.createStructure(
                        "source",
                        ASSyntax.createAtom(type.getKey()),
                        ASSyntax.createAtom(apparatusName)
                )
        );

        return out;
    }


    private List<Literal> getInteroceptions() {
        return interoceptions;
    }


    private List<Literal> getProprioceptions() {
        return proprioceptions;
    }


    private List<Literal> getExteroceptions() {
        return exteroceptions;
    }


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


    private void abolishProprioceptions() {
        proprioceptions.clear();
    }


    private void abolishInteroceptions() {
        interoceptions.clear();
    }


    private void abolishExteroceptions() {
        exteroceptions.clear();
    }


    private void loadPercepts(JSONObject bodyResponse) {

        if (!bodyResponse.has("percepts")
                || bodyResponse.isNull("percepts")) {
            return;
        }

        JSONObject percepts = bodyResponse.getJSONObject("percepts");

        addPerceptsByPerceptionsType(
                percepts,
                PerceptionType.EXTEROCEPTION
        );

        addPerceptsByPerceptionsType(
                percepts,
                PerceptionType.INTEROCEPTION
        );

        addPerceptsByPerceptionsType(
                percepts,
                PerceptionType.PROPRIOCEPTION
        );
    }


    private void addPerceptsByPerceptionsType(
            JSONObject perceptions,
            PerceptionType perceptionType
    ) {

        if (!perceptions.has(perceptionType.getKey())) {
            return;
        }

        JSONArray filteredPerceptionsByType =
                perceptions.getJSONArray(perceptionType.getKey());

        for (int i = 0; i < filteredPerceptionsByType.length(); i++) {

            JSONObject jsonObject =
                    filteredPerceptionsByType.getJSONObject(i);

            if (!jsonObject.has("percept")) {
                continue;
            }

            Literal belief =
                    neck.util.Util.JSONObjectToLiteral(
                            jsonObject,
                            "percept"
                    );

            if (jsonObject.has("args")) {

                JSONArray termsArgs =
                        jsonObject.getJSONArray("args");

                belief =
                        neck.util.Util.addJSONArrayAsTermsInLiteral(
                                belief,
                                termsArgs
                        );
            }

            addPercept(belief, perceptionType);
        }
    }


    private void addPercept(
            Literal literal,
            PerceptionType type
    ) {

        Literal annotated =
                getLiteralWithSourceBBAnnotation(literal, type);

        switch (type) {

            case EXTEROCEPTION ->
                    exteroceptions.add(annotated);

            case INTEROCEPTION ->
                    interoceptions.add(annotated);

            case PROPRIOCEPTION ->
                    proprioceptions.add(annotated);
        }
    }


    /* ============================================================
       CONNECTION / INTEROCEPTION
       ============================================================ */

    private void loadConnectionInfo(JSONObject bodyResponse) {

        /*
         * Compatibility with the current serial protocol.
         *
         * Other concrete Apparatus implementations are not required
         * to provide the "port" field.
         */
        if (!bodyResponse.has("port")) {
            return;
        }

        Literal litINFO = ASSyntax.createLiteral("port");

        Term portStatus =
                ASSyntax.createAtom(
                        bodyResponse.getString("port")
                );

        Term portAddress =
                ASSyntax.createString(
                        getAddress() == null
                                ? ""
                                : getAddress()
                );

        Term apparatus;
        Term apparatusID;

        if (bodyResponse.has("apparatus")
                && bodyResponse.has("apparatusID")) {

            apparatus =
                    neck.util.Util.stringToAtom(
                            bodyResponse.getString("apparatus")
                    );

            apparatusID =
                    ASSyntax.createNumber(
                            bodyResponse.getLong("apparatusID")
                    );

        } else {

            apparatus =
                    neck.util.Util.stringToAtom("unknown");

            apparatusID =
                    ASSyntax.createNumber(0);
        }

        litINFO.addTerm(portStatus);
        litINFO.addTerm(portAddress);
        litINFO.addTerm(apparatus);
        litINFO.addTerm(apparatusID);

        addPercept(
                litINFO,
                PerceptionType.INTEROCEPTION
        );
    }


    /* ============================================================
       DESIRES / TRIEBS
       ============================================================ */

    public List<Literal> getDesires() {
        return desires;
    }


    private void loadDesires(JSONObject bodyResponse) {

        desires.clear();

        if (!bodyResponse.has("triebs")
                || bodyResponse.isNull("triebs")) {
            return;
        }

        JSONArray bodyDesires =
                bodyResponse.getJSONArray("triebs");

        for (int i = 0; i < bodyDesires.length(); i++) {

            JSONObject desire =
                    bodyDesires.getJSONObject(i);

            if (!desire.has("trieb")) {
                continue;
            }

            Literal newDesire =
                    neck.util.Util.JSONObjectToLiteral(
                            desire,
                            "trieb"
                    );

            if (desire.has("args")) {

                JSONArray argsDesire =
                        desire.getJSONArray("args");

                newDesire =
                        neck.util.Util.addJSONArrayAsTermsInLiteral(
                                newDesire,
                                argsDesire
                        );
            }

            desires.add(newDesire);
        }
    }


    /* ============================================================
       TACIT KNOWLEDGE / KNOW-HOW
       ============================================================ */

    public void loadTacitKnowledge() {

        apparatusPlans.clear();

        JSONObject jsonObject = getKnowHow();

        if (jsonObject == null
                || !jsonObject.has("knowHow")) {
            return;
        }

        JSONArray knowHow =
                jsonObject.getJSONArray("knowHow");

        for (int i = 0; i < knowHow.length(); i++) {

            JSONObject skillObj =
                    knowHow.getJSONObject(i);

            if (!skillObj.has("context")
                    || !skillObj.has("skill")
                    || !skillObj.has("plan")) {
                continue;
            }

            String context =
                    skillObj.isNull("context")
                            ? null
                            : skillObj.get("context").toString();

            String trigger =
                    skillObj.isNull("skill")
                            ? null
                            : skillObj.get("skill").toString();

            String planBody =
                    skillObj.isNull("plan")
                            ? null
                            : skillObj.get("plan").toString();

            if ("FILE".equals(context)) {

                // TODO load from file

            } else if ("URL".equals(context)) {

                // TODO load from URL

            } else {

                addPlan(
                        trigger,
                        context,
                        planBody
                );
            }
        }
    }


    private void addPlan(
            String trigger,
            String context,
            String body
    ) {

        if (context == null || context.isBlank()) {
            context = "true";
        }

        if (body == null || body.isBlank()) {
            body = "true";
        }

        if (trigger == null || trigger.isBlank()) {
            logger.severe("BAD formatted skill: empty trigger");
            return;
        }

        String stringPlan =
                "+!" + trigger
                        + " : "
                        + context
                        + " <- "
                        + body
                        + ".";

        try {

            Plan plan =
                    ASSyntax.parsePlan(stringPlan);

            if (plan != null) {
                apparatusPlans.add(plan);
            }

        } catch (Exception ex) {

            logger.severe(
                    "BAD formatted Skill: "
                            + stringPlan
            );
        }
    }


    public Plan[] getPlans() {

        if (apparatusPlans.isEmpty()) {
            return null;
        }

        return apparatusPlans.toArray(new Plan[0]);
    }
}