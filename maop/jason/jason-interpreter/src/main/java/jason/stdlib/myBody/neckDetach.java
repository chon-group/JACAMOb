package jason.stdlib.myBody;

import jacamo.infra.JaCaMoAgArch;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Term;
import neck.Body;

public class neckDetach extends DefaultInternalAction {
    private Term appName = null;

    @Override
    public int getMinArgs() {return 1;}

    @Override
    public int getMaxArgs() {return 1;}

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (args.length == 1 && args[0].isAtom()){
            this.appName = args[0];
        }
        else {
            throw JasonException.createWrongArgument(this, "ERROR: Consult https://github.com/chon-group/JACAMOb/wiki");
        }
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        /* EXPECTED
            .mybody.neckDetach(APPARATUSNAME)
         */
        checkArguments(args);

        try {
            if(currentAgtBody(ts) != null){
                return currentAgtBody(ts).detachApparatusByName(getAppNameAsString());
            }
            return false;
        } catch (Exception ex) {
            throw JasonException.createWrongArgument(
                    this,
                    "Failed to detach apparatus at " + getAppNameAsString()
            );
        }

    }


    private String getAppNameAsString(){
        if(appName == null) return null;
        return this.appName.toString();
    }


    private static Body currentAgtBody(TransitionSystem ts) {
        AgArch arch = ts.getAgArch().getFirstAgArch();
        while (arch != null) {
            if (arch instanceof JaCaMoAgArch jcm) {
                return jcm.getAgtBody(); // <- Body do agente atual
            }
            arch = arch.getNextAgArch();
        }
        throw new IllegalStateException("JaCaMoAgArch não encontrado na cadeia de AgArch");
    }
}
