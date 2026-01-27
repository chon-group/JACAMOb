package neck;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.RevisionFailedException;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.*;
import org.json.JSONObject;

import java.util.*;

public class Body {
    Logger logger;
    //String[] apparatusAvailables;
    List<String> attachedAppAddress = new ArrayList<>();
    List<String> attachedAppName = new ArrayList<>();
    //Integer apparatusAvailablesInt = 0;
    Apparatus[] apparatus = new Apparatus[128];
    String bodyName;
    public static final Atom BODY_NAMESPACE = ASSyntax.createAtom("myBody");
    private static final String SOURCE_FUNCTOR = "source";

    public Body(String bodyName) {
        this.bodyName = bodyName;
        this.logger = Logger.getLogger(bodyName);
        logger.info("Embodying...");
    }

    public boolean attachApparatus(Apparatus implementation, String apparatusName) {
        String address = implementation.getAddress();
        int idx = attachedAppAddress.size();

        if (address != null && attachedAppAddress.contains(address)) {
            logger.info("Apparatus in " + address + " already attached");
            return false;
        }

        if (apparatusName != null && attachedAppName.contains(apparatusName)) {
            logger.info("Apparatus " + apparatusName + " is already attached");
            return false;
        }

        if (idx >= apparatus.length) {
            logger.severe("Capacidade de apparatus esgotada (" + apparatus.length + ")");
            return false;
        }

        if (address != null && apparatusName != null){
            attachedAppAddress.add(address);
            attachedAppName.add(apparatusName);
            apparatus[idx] = implementation;
            apparatus[idx].setApparatusName(apparatusName);
            return true;
        }

        return false;
    }

    private List<Literal> getPercepts(){
        List<Literal> list = new ArrayList<>();
        for(int i = 0; i < attachedAppName.size(); i++){
            if(!apparatus[i].getStatus()) logger.log(Level.SEVERE,"Apparatus ["+apparatus[i].getApparatusName()+"] is "+apparatus[i].getConnectionStatus());
            apparatus[i].bodyPerception();
            list.addAll(apparatus[i].getAllPerceptions());
        }
        return list;
    }

    public void updatePercepts(TransitionSystem ts) {
        try {
            // 1) Novas percepções (já com as anotações source(i|p|e))
            List<Literal> incoming = getPercepts();
            Set<String> incomingKeys = new HashSet<>();
            for (Literal lit : incoming) {
                incomingKeys.addAll(keysFor(lit));
            }

            // 2) Coleta crenças atuais com source(i|p|e) e identifica as que devem sair
            List<Literal> toDelete = new ArrayList<>();
            Set<String> currentKeys = new HashSet<>();
            for (Literal belief : ts.getAg().getBB()) {
                if(!isFomBodyNS(belief)){continue;}
                Set<String> ks = keysFor(belief);
                currentKeys.addAll(ks);
                for (String k : ks) {
                    if (!incomingKeys.contains(k)) {
                        toDelete.add(literalFromKey(belief, k));
                    }
                }
            }

            // 3) Remove as que sumiram/mudaram
            for (Literal b : toDelete) {
                ts.getAg().delBel(b);
            }

            // 4) Adiciona apenas o que é novo
            for (Literal lit : incoming) {
                String k = keyFor(lit);
                if (!currentKeys.contains(k)) {
                    ts.getAg().addBel(lit);
                }
            }

        } catch (RevisionFailedException e) {
            throw new RuntimeException(e);
        }
    }

    /** Chave canônica: crença + termos + source(type,apparatus)*/
    private String keyFor(Literal l) {
        StringBuilder sb = new StringBuilder();
        sb.append(l.getFunctor()).append('(');
        for (int i = 0; i < l.getArity(); i++) {
            if (i > 0) sb.append(',');
            sb.append(l.getTerm(i).toString());
        }
        sb.append(')');
        sb.append("#src=").append(extractSource(l));
        sb.append("#app=").append(extractApparatus(l));
        return sb.toString();
    }

    /** Pega o literal source(Type,App). */
    private Literal getSource2(Literal l) {
        for (Term ann : l.getAnnots()) {
            if (ann.isLiteral()) {
                Literal a = (Literal) ann;
                if (SOURCE_FUNCTOR.equals(a.getFunctor()) && a.getArity() == 2) {
                    return a;
                }
            }
        }
        throw new IllegalStateException("Percept sem annotation source(Type,App): " + l);
    }

    private String extractSource(Literal l) {
        return getSource2(l).getTerm(0).toString(); // Type
    }

    private String extractApparatus(Literal l) {
        return getSource2(l).getTerm(1).toString(); // App
    }


    public void act(String CMD){
        for(int i = 0; i < attachedAppName.size(); i++){
            apparatus[i].act(CMD);
            logger.log(Level.SEVERE,"[body] actinging "+CMD+" in "+apparatus[i].getAddress());
        }
    }


    private boolean isFomBodyNS(Literal b){
        if (b.getNS() == BODY_NAMESPACE){
            return true;
        }else{
            return false;
        }
    }

    private Set<String> keysFor(Literal l) {
        String base = baseKey(l); // functor(termos...)

        Set<String> keys = new LinkedHashSet<>();
        for (SrcApp sa : extractSourcePairs(l)) {
            keys.add(base + "#src=" + sa.src + "#app=" + sa.app);
        }

        // fallback de segurança (se não tiver source/2 por algum motivo)
        if (keys.isEmpty()) {
            keys.add(base + "#src=unknown#app=unknown");
        }
        return keys;
    }

    private static class SrcApp {
        final String src;
        final String app;
        SrcApp(String src, String app) { this.src = src; this.app = app; }
    }

    private List<SrcApp> extractSourcePairs(Literal l) {
        List<SrcApp> out = new ArrayList<>();

        // pega TODAS as annotations e filtra source(...)
        for (Term annT : l.getAnnots()) {
            if (!(annT instanceof Literal ann)) continue;

            if (!"source".equals(ann.getFunctor())) continue;

            // source(Type,App)
            if (ann.getArity() >= 2) {
                String src = ann.getTerm(0).toString();
                String app = ann.getTerm(1).toString();
                out.add(new SrcApp(src, app));
                continue;
            }

        }

        return out;
    }

    private String baseKey(Literal l) {
        StringBuilder sb = new StringBuilder();
        sb.append(l.getFunctor()).append('(');
        for (int i = 0; i < l.getArity(); i++) {
            if (i > 0) sb.append(',');
            sb.append(l.getTerm(i).toString());
        }
        sb.append(')');
        return sb.toString();
    }

    private Literal literalFromKey(Literal template, String key) {
        // key:  "...#src=TYPE#app=APPARATUS"
        String src = extractBetween(key, "#src=", "#app=");
        String app = extractAfter(key, "#app=");

        Literal out = (Literal) template.clone();
        out.clearAnnots();

        // source(TYPE,APPARATUS)
        out.addAnnot(
                ASSyntax.createStructure(
                        "source",
                        ASSyntax.createAtom(src),
                        ASSyntax.createAtom(app)
                )
        );
        return out;
    }

    private String extractBetween(String s, String a, String b) {
        int ia = s.indexOf(a);
        if (ia < 0) return "";
        ia += a.length();
        int ib = s.indexOf(b, ia);
        if (ib < 0) return s.substring(ia);
        return s.substring(ia, ib);
    }

    private String extractAfter(String s, String a) {
        int ia = s.indexOf(a);
        if (ia < 0) return "";
        return s.substring(ia + a.length());
    }

    public boolean detachApparatusByName(String apparatusName) {
        if (apparatusName == null) return false;
        int n = attachedAppName.size();
        for (int i = 0; i < n; i++) {
            if (apparatus[i] != null && apparatusName.equals(apparatus[i].getApparatusName())) {
                attachedAppName.remove(i);
                attachedAppAddress.remove(i);
                int newSize = attachedAppName.size();
                for (int k = i; k < newSize; k++) {
                    apparatus[k] = apparatus[k + 1];
                }
                apparatus[newSize] = null;
                return true;
            }
        }
        return false;
    }
}