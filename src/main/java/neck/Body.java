package neck;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.RevisionFailedException;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import org.json.JSONObject;

import java.util.*;

public class Body {
    Logger logger;
    String[] apparatusAvailables;
    List<String> apparatusAttached = new ArrayList<>();
    Integer apparatusAvailablesInt = 0;
    Apparatus[] apparatus = new Apparatus[128];
    String bodyName;

    private Body(){
       // System.out.println("CRIANDO UM CORPO...");
    }

    public Body(String bodyName) {
        this();
        this.bodyName = bodyName;
        this.logger = Logger.getLogger(bodyName);

    }

    public void attachApparatus(Apparatus implementation, String apparatusName) {
        String address = implementation.getAddress();

        if (address != null && apparatusAttached.contains(address)) {
            System.out.println("Apparatus in " + address + " already attached");
            return;
        }

        int idx = apparatusAttached.size();
        if (idx >= apparatus.length) {
            throw new IllegalStateException("Capacidade de apparatus esgotada (" + apparatus.length + ")");
        }

        apparatus[idx] = implementation;
        if (address != null) apparatusAttached.add(address);
        apparatus[idx].setApparatusName(apparatusName);
    }

    private List<Literal> getPercepts(){
        List<Literal> list = new ArrayList<>();
        for(int i = 0; i < apparatusAttached.size(); i++){
            if(apparatus[i].getStatus()){
                apparatus[i].bodyPerception();
                list.addAll(apparatus[i].getAllPerceptions());
            }else{
                logger.log(Level.SEVERE,"Apparatus ["+apparatusAttached.get(i).toString()+"] not OK");
            }
        }
        return list;
    }
    public void updatePercepts(TransitionSystem ts) {
        try {
            // 1) Novas percepções (já com as anotações source(i|p|e))
            List<Literal> incoming = getPercepts();
            Set<String> incomingKeys = new HashSet<>();
            for (Literal lit : incoming) {
                incomingKeys.add(keyFor(lit));
            }

            // 2) Coleta crenças atuais com source(i|p|e) e identifica as que devem sair
            List<Literal> toDelete = new ArrayList<>();
            Set<String> currentKeys = new HashSet<>();
            for (Literal belief : ts.getAg().getBB()) {
                if (!isFromKnownSource(belief)) continue; // só mexe nas crenças dessas origens
                String k = keyFor(belief);
                currentKeys.add(k);
                if (!incomingKeys.contains(k)) {
                    toDelete.add(belief); // estava antes e não veio agora → remover
                }
            }

            // 3) Remove as que sumiram
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

    /** Verifica se a crença tem anotação source(i|p|e). */
    private boolean isFromKnownSource(Literal belief) {
        if (!belief.hasAnnot()) return false;
        for (Term ann : belief.getAnnots()) {
            if (ann.isStructure()) {
                Structure s = (Structure) ann;
                if ("source".equals(s.getFunctor()) && s.getArity() == 1) {
                    String src = s.getTerm(0).toString(); // "interoception"/"proprioception"/"exteroception"
                    if ("interoception".equals(src) || "proprioception".equals(src) || "exteroception".equals(src)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Chave canônica: functor + termos + source(...) — garante comparação estável. */
    private String keyFor(Literal l) {
        StringBuilder sb = new StringBuilder();
        sb.append(l.getFunctor()).append('(');
        for (int i = 0; i < l.getArity(); i++) {
            if (i > 0) sb.append(',');
            sb.append(l.getTerm(i).toString());
        }
        sb.append(')');
        sb.append("#src=").append(extractSource(l));
        return sb.toString();
    }

    private String extractSource(Literal l) {
        if (l.hasAnnot()) {
            for (Term ann : l.getAnnots()) {
                if (ann.isStructure()) {
                    Structure s = (Structure) ann;
                    if ("source".equals(s.getFunctor()) && s.getArity() == 1) {
                        return s.getTerm(0).toString();
                    }
                }
            }
        }
        return ""; // nenhum source
    }

    public void act(String CMD){
        for(int i = 0; i < apparatusAttached.size(); i++){
            apparatus[i].act(CMD);
            logger.log(Level.SEVERE,"[body] actinging "+CMD+" in "+apparatus[i].getAddress());
        }
    }


}