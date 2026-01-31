package jason.stdlib.myBody;

import jacamo.infra.JaCaMoAgArch;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Term;
import neck.Body;
import neck.model.BodyResponse;



public class act extends DefaultInternalAction {

    private BodyResponse bodyResponse    = null;
    private Term    actionTerm          = null;
    private Atom    apparatusName       = null;
    private Term    replyRequested      = null;

    @Override
    public int getMinArgs() {return 1;}

    @Override
    public int getMaxArgs() {return 3;}

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments

        /* EXPECTED -->
            .myBody.act(action(arg1,arg2,argn));
            .myBody.act(actionWithoutArgs);
        */
        if (args.length == 1 && args[0].isLiteral()){
            this.actionTerm = args[0];
            this.apparatusName = null;
            this.replyRequested = null;
            return;
        }

        /* EXPECTED -->
            .myBody.act(action(arg1,arg2,argn),REPLY);
            .myBody.act(actionWithoutArgs,REPLY);
        */
        if (args.length == 2 && args[0].isLiteral() && args[1].isVar()){
            this.actionTerm = args[0];
            this.apparatusName = null;
            this.replyRequested = args[1];
            return;
        }

        /* EXPECTED -->
            .myBody.act(action(arg1,arg2,argn),apparatus1);
            .myBody.act(actionWithoutArgs,apparatus1);
        */
        if (args.length == 2 && args[0].isLiteral() && args[1].isAtom()){
            this.actionTerm = args[0];
            this.apparatusName = ASSyntax.createAtom(args[1].toString());
            this.replyRequested = null;
            return;
        }

        /* EXPECTED -->
            .myBody.act(action(arg1,arg2,argn),apparatus1,REPLY);
            .myBody.act(actionWithoutArgs,apparatus1,REPLY);
        */
        if (args.length == 3 && args[0].isLiteral() && args[1].isAtom() && args[2].isVar()){
            this.actionTerm = args[0];
            this.apparatusName = ASSyntax.createAtom(args[1].toString());
            this.replyRequested = args[2];
            return;
        }

        throw JasonException.createWrongArgument(this, "ERROR: Consult https://github.com/chon-group/JACAMOb/wiki");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        checkArguments(args);

        bodyResponse = currentAgtBody(ts).act(this.actionTerm,this.apparatusName);

        if (bodyResponse != null){
            if (replyRequested != null){
                un.unifies(replyRequested,bodyResponse.toTerm());
                ts.getLogger().fine("Action "+this.actionTerm+" replied "+bodyResponse);
                return true;
            }

            if (bodyResponse == BodyResponse.EXECUTED) ts.getLogger().fine("Action "+this.actionTerm+" replied "+bodyResponse);
            else if (bodyResponse == BodyResponse.UNCHANGED) ts.getLogger().info("Action "+this.actionTerm+" replied "+bodyResponse);
            else ts.getLogger().severe("Action "+this.actionTerm+" replied "+bodyResponse);

            if((bodyResponse == BodyResponse.EXECUTED) || (bodyResponse == BodyResponse.UNCHANGED)) return true;
            else return false;
        }
        return false;
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
