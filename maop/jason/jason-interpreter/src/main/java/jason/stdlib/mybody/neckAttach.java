package jason.stdlib.mybody;

import jacamo.infra.JaCaMoAgArch;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import neck.Apparatus;
import neck.Body;
import neck.DefaultApparatus;

public class neckAttach extends DefaultInternalAction {

    private Term appName = null;
    private Term appAddress = null;

    @Override
    public int getMinArgs() {return 1;}

    @Override
    public int getMaxArgs() {return 2;}

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments
        if (args.length == 2 && args[0].isLiteral() && args[1].isString()){
            this.appName = args[0];
            this.appAddress = (StringTerm) args[1];
        }
        else if (args.length == 1 && args[0].isString()){
            this.appName = null;
            this.appAddress = (StringTerm) args[0];
        }
        else {
            throw JasonException.createWrongArgument(this, "ERROR: Consult https://github.com/chon-group/JACAMOb/wiki");
        }
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        /* EXPECTED
            .mybody.neckAttach(APPARATUSNAME,ADDRESS)
            .mybody.neckAttach(ADDRESS)
         */
        checkArguments(args);

        try {
            if(currentAgtBody(ts) != null){
                Apparatus apparatus = null;
                if (getAppAddressAsString() != null) apparatus = new DefaultApparatus(getAppAddressAsString());
                if(getAppNameAsString()!=null){
                    return currentAgtBody(ts).attachApparatus(apparatus,getAppNameAsString());
                }else {
                    return currentAgtBody(ts).attachApparatus(apparatus,apparatus.getHwAppName());
                }
            }
                return false;
        } catch (Exception ex) {
            throw JasonException.createWrongArgument(
                    this,
                    "Failed to attach apparatus at " + getAppAddressAsString()
            );
        }

    }

    private String getAppAddressAsString(){
            return this.appAddress.toString();
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
