package neck;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jason.JasonException;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.*;
import neck.model.BodyResponse;

public class Body {
    private Logger logger;
    private List<String> attachedAppAddress = new ArrayList<>();
    private List<String> attachedAppName = new ArrayList<>();
    private Apparatus[] apparatus = new Apparatus[128];
    private String bodyName;
    public static final Atom BODY_NAMESPACE = ASSyntax.createAtom("myBody");
    //private static final String SOURCE_FUNCTOR = "source";

    public Body(String bodyName) {
        this.bodyName = bodyName;
        this.logger = Logger.getLogger(bodyName);
        logger.info("Embodying...");
    }

    public boolean attachApparatus(Apparatus implementation){
        return attachApparatus(implementation,null);
    }

    public boolean attachApparatus(Apparatus implementation, String apparatusName) {
        String address = implementation.getAddress();

        try{
            if (!"executed".equals(implementation.embody().getString("bodyResponse"))) {
                logger.severe("Failed to embody Apparatus [" + apparatusName + "]");
                return false;
            }
        } catch (Exception e) {
            logger.severe("Failed to embody Apparatus [" + apparatusName + "] \n\t"+e.toString());
            return false;
        }

        if (apparatusName == null) apparatusName = implementation.getHwAppName();

        int idx = attachedAppAddress.size();

        if (idx >= apparatus.length) {
            logger.severe("Capacidade de apparatus esgotada (" + apparatus.length + ")");
            return false;
        }

        if (address != null && attachedAppAddress.contains(address)) {
            logger.info("Apparatus in " + address + " already attached");
            return false;
        }

        if (apparatusName != null && attachedAppName.contains(apparatusName)) {
            logger.info("Has an apparatus with same name: " + apparatusName);
            for(int i=2; i<apparatus.length; i++){
                if(!attachedAppName.contains(String.valueOf(apparatusName+i))){
                    apparatusName = String.valueOf(apparatusName+i);
                    i = apparatus.length;
                }
            }
            //return false;
        }

        if (address != null && apparatusName != null && implementation.getStatus()){
            attachedAppAddress.add(address);
            attachedAppName.add(apparatusName);
            apparatus[idx] = implementation;
            apparatus[idx].setApparatusName(apparatusName);
            //apparatus[idx].loadPlans();
            logger.info("Apparatus ["+apparatusName+"] was attached!");
            return true;
        }
        logger.info("ERROR when attaching the Apparatus ["+apparatusName+"] at ["+address+"]");
        return false;
    }

    public boolean detachApparatusByName(String apparatusName) {
        if (apparatusName == null) return false;
        int n = attachedAppName.size();
        for (int i = 0; i < n; i++) {
            if (apparatus[i] != null && apparatusName.equals(apparatus[i].getApparatusName())) {
                apparatus[i].detach();
                attachedAppName.remove(i);
                attachedAppAddress.remove(i);
                int newSize = attachedAppName.size();
                for (int k = i; k < newSize; k++) {
                    apparatus[k] = apparatus[k + 1];
                }
                apparatus[newSize] = null;
                logger.info("Apparatus ["+apparatusName+"] was detached!");
                return true;
            }
        }
        logger.severe("ERROR in detaching Apparatus ["+apparatusName+"]");
        return false;
    }

    public void perceive(TransitionSystem transitionSystem) {
        logger.fine("Body update percepts, starting...");

        List<Literal> listOfPerceptions = new ArrayList<>();
        List<Literal> listOfDesires = new ArrayList<>();
        /* percorre todos os apparatus executando o sense do apparatus pega percepcoes e desejos*/
        for(int i = 0; i < attachedAppName.size(); i++){
            if(!apparatus[i].getStatus()){
                logger.log(Level.SEVERE,"Apparatus ["+apparatus[i].getApparatusName()+"] is "+apparatus[i].getConnectionStatus());
                continue;
            }else{
                apparatus[i].sense();
                listOfPerceptions.addAll(apparatus[i].getAllPerceptions());
                listOfDesires.addAll(apparatus[i].getAllDesires());
            }
        }

        /* função de revisão de percepções */
        bodyPerceptionRevisionFuntion(transitionSystem,listOfPerceptions);

        for (Literal desire : listOfDesires) {
            Trigger trigger = new Trigger(Trigger.TEOperator.add, Trigger.TEType.achieve, desire);
            Event ev = new Event(trigger);
            transitionSystem.updateEvents(ev);
            logger.fine("\t NEW desire..." + ev.toString());
        }
        listOfDesires = null;

       logger.fine("Body update percepts, finished...");
    }



    public BodyResponse act(Term actionTerm, Atom apparatusName){
        Apparatus apparatus = null;

        if(apparatusName == null) apparatus = getApparatusBySupportedAction(actionTerm);
        else apparatus = getApparatusByName(apparatusName);

        if(apparatus == null) return BodyResponse.UNKNOWN;

        return apparatus.act(actionTerm);
    }

    private static class SrcApp {
        final String src;
        final String app;
        SrcApp(String src, String app) { this.src = src; this.app = app; }
    }

//    private List<SrcApp> extractSourcePairs(Literal l) {
//        List<SrcApp> out = new ArrayList<>();
//
//        // pega TODAS as annotations e filtra source(...)
//        for (Term annT : l.getAnnots()) {
//            if (!(annT instanceof Literal ann)) continue;
//
//            if (!"source".equals(ann.getFunctor())) continue;
//
//            // source(Type,App)
//            if (ann.getArity() >= 2) {
//                String src = ann.getTerm(0).toString();
//                String app = ann.getTerm(1).toString();
//                out.add(new SrcApp(src, app));
//                continue;
//            }
//
//        }
//
//        return out;
//    }

//    private String baseKey(Literal l) {
//        StringBuilder sb = new StringBuilder();
//        sb.append(l.getFunctor()).append('(');
//        for (int i = 0; i < l.getArity(); i++) {
//            if (i > 0) sb.append(',');
//            sb.append(l.getTerm(i).toString());
//        }
//        sb.append(')');
//        return sb.toString();
//    }

//    private Literal literalFromKey(Literal template, String key) {
//        // key:  "...#src=TYPE#app=APPARATUS"
//        String src = extractBetween(key, "#src=", "#app=");
//        String app = extractAfter(key, "#app=");
//
//        Literal out = (Literal) template.clone();
//        out.clearAnnots();
//
//        // source(TYPE,APPARATUS)
//        out.addAnnot(
//                ASSyntax.createStructure(
//                        "source",
//                        ASSyntax.createAtom(src),
//                        ASSyntax.createAtom(app)
//                )
//        );
//        return out;
//    }

//    private String extractBetween(String s, String a, String b) {
//        int ia = s.indexOf(a);
//        if (ia < 0) return "";
//        ia += a.length();
//        int ib = s.indexOf(b, ia);
//        if (ib < 0) return s.substring(ia);
//        return s.substring(ia, ib);
//    }
//
//    private String extractAfter(String s, String a) {
//        int ia = s.indexOf(a);
//        if (ia < 0) return "";
//        return s.substring(ia + a.length());
//    }

    public Plan[] getPlansByApparatusName(String apparatusName){
        if (apparatusName == null) return null;
        int n = this.attachedAppName.size();
        for (int i = 0; i < n; i++) {
            if (this.apparatus[i] != null && apparatusName.equals(this.apparatus[i].getApparatusName())) {
                return this.apparatus[i].getPlans();
            }
        }
        return null;
    }


    private Apparatus getApparatusByName(Atom appName){
        /* PODERIA SER ATOM */
        if (!attachedAppName.contains(appName.getFunctor())) return null;
        for (int i = 0; i < this.attachedAppName.size(); i++) {
            if(apparatus[i].getApparatusName().equals(appName.getFunctor()))
                return apparatus[i];
        }
        return null;
    }

    private Apparatus getApparatusBySupportedAction(Term actionName){
        for (int i = 0; i < this.attachedAppName.size(); i++) {
            if(apparatus[i].supportsAction(neck.util.Util.getFunctor(actionName)))
                return apparatus[i];
        }
        return null;
    }

    /* helpers */
    private void bodyPerceptionRevisionFuntion(TransitionSystem transitionSystem, List<Literal> listOfPerceptions){

        // Lista as crenças atuais de myBody::
        List<Literal> currentBodyBB = new ArrayList<>();
        for (Literal b : transitionSystem.getAg().getBB()) if (b.getNS() == BODY_NAMESPACE) currentBodyBB.add(b);

        /* unifica as percepcoes de difentes apparatus */
        List<Literal> revisedList = new ArrayList<>();
        for (Literal perception : listOfPerceptions) {

            Literal merged = null;

            // Procura no out a mesma crença, desconsiderando as annotations
            for (Literal current : revisedList) {
                if (current.copy().clearAnnots().equals(perception.copy().clearAnnots())) {
                    merged = current;
                    break;
                }
            }

            // Ainda não existe: inclui a percepção
            if (merged == null) {
                revisedList.add(perception.copy());
                continue;
            }

            // Já existe: acrescenta as annotations
            for (Term annot : perception.getAnnots()) {
                if (!merged.hasAnnot(annot)) {
                    merged.addAnnot(annot);
                }
            }
        }
        listOfPerceptions = null;

        /* Percorre a lista de crenças myBody:: na BB, atualizando removendo crenças ou anotações antigas*/
        removeOldPerceptions(currentBodyBB, revisedList, transitionSystem);
        /* Percorre a lista de percepcoes advindas do corpo - adiciona as novas*/
        addNewPerceptions(currentBodyBB, revisedList, transitionSystem);

    }

    /* genrenciamento das crencas na BB */
    private void removeOldPerceptions(List<Literal> bodyBeliefs, List<Literal> perceived, TransitionSystem ts){
        for (Literal literalInBB : bodyBeliefs){
            if(removeCompletelyOldPerception(literalInBB, perceived, ts)){continue;}
            else if(removeAnnotsOfOldPerception(literalInBB,perceived, ts)){ continue;}
        }
    }

    private boolean removeAnnotsOfOldPerception(Literal beliefInBB, List<Literal> incoming, TransitionSystem transitionSystem) {
        //List<Literal> out = new ArrayList<>();
        Literal beliefBase = beliefInBB.copy().clearAnnots();
        for (Literal perception : incoming) {
            Literal perceptionBase = perception.copy().clearAnnots();

            // Só interessa se for a mesma crença-base
            if (!beliefBase.equals(perceptionBase)) continue;

            // Cria a crença-base que receberá somente as annotations que desapareceram
            Literal partial = beliefInBB.copy().clearAnnots();

            for (Term annot : beliefInBB.getAnnots()) if (!perception.hasAnnot(annot)) partial.addAnnot(annot);

            // Só inclui se alguma annotation desapareceu
            if (partial.hasAnnot()){
                logger.fine(" -" + partial.toString()) ;
                transitionSystem.getAg().getBB().remove(partial);

                Trigger te = new Trigger(Trigger.TEOperator.del, Trigger.TEType.belief, partial);
                Event ev = new Event(te, Intention.EmptyInt);
                transitionSystem.getC().addEvent(ev);
                return true;
            }
        }
        return false;
    }

    /* crença não existe mais no corpo */
    private boolean removeCompletelyOldPerception(Literal beliefInBB, List<Literal> incoming, TransitionSystem transitionSystem) {
        Literal inPerceptionIncomming = null;
        Literal inBeliefBase = beliefInBB.copy().clearAnnots();

        for (Literal perception : incoming) {
            inPerceptionIncomming= perception.copy().clearAnnots();
            if (inBeliefBase.equals(inPerceptionIncomming)) return false;
        }

        logger.fine(" -" + beliefInBB.toString());
        transitionSystem.getAg().getBB().remove(beliefInBB);

        Trigger te = new Trigger(Trigger.TEOperator.del, Trigger.TEType.belief, beliefInBB);
        Event ev = new Event(te, Intention.EmptyInt);
        transitionSystem.getC().addEvent(ev);

        return true;
    }

    private void addNewPerceptions(List<Literal> bodyBeliefs, List<Literal> perceived, TransitionSystem ts){
        /* Percorre a lista de percepcoes advindas do corpo - adiciona as novas*/
        for(Literal perception : perceived){
            if(isNewPerception(bodyBeliefs, perception)){
                try {
                    logger.fine(" +" + perception.toString());
                    ts.getAg().getBB().add(perception);

                    Trigger te = new Trigger(Trigger.TEOperator.add, Trigger.TEType.belief, perception);
                    Event ev = new Event(te, Intention.EmptyInt);
                    ts.getC().addEvent(ev);
                }
                catch (JasonException e) {throw new RuntimeException(e);}
            }
        }
    }

    private boolean isNewPerception(List<Literal> current, Literal perception) {
        Literal beliefInBB = null;
        Literal perceptionBase = perception.copy().clearAnnots();
        for (Literal belief : current) {
            beliefInBB = belief.copy().clearAnnots();
            if (perceptionBase.equals(beliefInBB)) return false;
        }
        return true;
    }
}