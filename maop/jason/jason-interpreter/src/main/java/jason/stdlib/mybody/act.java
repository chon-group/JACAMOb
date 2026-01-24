package jason.stdlib.mybody;

import jacamo.infra.JaCaMoAgArch;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;

public class act extends DefaultInternalAction {
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        ts.getAgArch().getNextAgArch().realWorldAct(args[0].toString());
        return true;
    }
}
