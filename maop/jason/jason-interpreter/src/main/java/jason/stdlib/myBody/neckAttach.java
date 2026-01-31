package jason.stdlib.myBody;

import jacamo.infra.JaCaMoAgArch;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;
import neck.Apparatus;
import neck.Body;
import neck.DefaultApparatus;

public class neckAttach extends DefaultInternalAction {

    private Term appName = null;
    private Term appAddress = null;
    private Term response = null;

    @Override
    public int getMinArgs() {return 1;}

    @Override
    public int getMaxArgs() {return 3;}

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args); // check number of arguments

        /* .mybody.neckAttach("/dev/ttyUSB0") */
        if (args.length == 1 && args[0].isString()){
            this.appName = null;
            this.appAddress = (StringTerm) args[0];
            this.response = null;
            return;
        }

        /* .mybody.neckAttach("/dev/ttyUSB0",Reply) */
        if (args.length == 2 && args[0].isString() && args[1].isVar()){
            this.appName = null;
            this.appAddress = (StringTerm) args[0];
            this.response = args[1];
            return;
        }

        /* .mybody.neckAttach(apparatus1,"/dev/ttyUSB0") */
        if (args.length == 2 && args[0].isLiteral() && args[1].isString()){
            this.appName = args[0];
            this.appAddress = (StringTerm) args[1];
            this.response = null;
            return;
        }

        /* .mybody.neckAttach(apparatus1,"/dev/ttyUSB0",Reply) */
        if (args.length == 3 && args[0].isLiteral() && args[1].isString() && args[2].isVar()){
            this.appName = args[0];
            this.appAddress = (StringTerm) args[1];
            this.response = args[2];
            return;
        }

        throw JasonException.createWrongArgument(this, "ERROR: Consult https://github.com/chon-group/JACAMOb/wiki");
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        checkArguments(args);

        if(!neck.util.Util.serialPortIsAvailable(getAppAddressAsString())) {
            ts.getLogger().severe("Serial port ["+getAppAddressAsString()+"] isn't available");
            return false;
        }

        try {
            if(currentAgtBody(ts) != null){
                Apparatus newApparatus = new DefaultApparatus(getAppAddressAsString());
                boolean attached = false;
                if(this.appName!=null) attached =  currentAgtBody(ts).attachApparatus(newApparatus,this.appName.toString());
                else attached = currentAgtBody(ts).attachApparatus(newApparatus);

                if (!attached){
                    return false;
                }else{
                    if (this.response != null) un.unifies(this.response, ASSyntax.createAtom(newApparatus.getApparatusName()));
                    return true;
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
