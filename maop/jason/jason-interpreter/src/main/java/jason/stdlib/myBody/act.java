package jason.stdlib.myBody;

import jacamo.infra.JaCaMoAgArch;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import neck.Body;
import neck.model.BodyResponse;



public class act extends DefaultInternalAction {

    private BodyResponse bdyResponse = BodyResponse.UNKNOWN;
    private Term    actionTerm = null;
    private String  apparatusName = null;

    @Override
    public int getMinArgs() {return 1;}

    @Override
    public int getMaxArgs() {return 2;}

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (args.length == 2 && args[0].isLiteral() && args[1].isAtom()){
            this.actionTerm = args[0];
            this.apparatusName = args[1].toString();
        }
        else if (args.length == 1 && args[0].isLiteral()){
            this.actionTerm = args[0];
            this.apparatusName = null;
        }
        else {
            throw JasonException.createWrongArgument(this, "ERROR: Consult https://github.com/chon-group/JACAMOb/wiki");
        }
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        bdyResponse = currentAgtBody(ts).act(this.actionTerm,this.apparatusName);

        if(bdyResponse != null) System.out.println(bdyResponse.toString());
        /* FAZER AINDA */
        return true;
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
