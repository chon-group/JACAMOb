package neck.internalActions;

import jacamo.infra.JaCaMoAgArch;
import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Plan;
import jason.asSyntax.Term;
import jason.asSyntax.StringTerm;
import neck.Body;

public class DetachApparatus extends DefaultInternalAction {

    private Term appName = null;
    private Term appAddress = null;

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    protected void checkArguments(Term[] args) throws JasonException {
        super.checkArguments(args);
        /* .myBody.detach(apparatus1) */
        if (args.length == 1 && args[0].isAtom()) {
            this.appName = args[0];
            this.appAddress = null;
            return;
        }

        /* .myBody.detach("/dev/ttyUSB0") */
        if (args.length == 1 && args[0].isString()) {
            this.appName = null;
            this.appAddress = args[0];
            return;
        }

        throw JasonException.createWrongArgument(
                this,
                "ERROR: Consult https://github.com/chon-group/JACAMOb/wiki"
        );
    }

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {

        checkArguments(args);
        try {
            Body body = currentAgtBody(ts);
            if (body == null) {return false;}

            String apparatusName;

            if (this.appName != null) {apparatusName = getAppNameAsString();}
            else {apparatusName = body.getApparatusNameByAddress(getAppAddressAsString());}

            if (apparatusName == null) {
                ts.getLogger().severe("Apparatus not found!");
                return false;
            }

            Plan[] plans = body.getPlansByApparatusName(apparatusName);

            if (plans != null) {

                for (int i = 0; i < plans.length; i++) {

                    try {
                        ts.getLogger().info("Removing from plan Library "+ plans[i].getLabel());
                        ts.getAg().getPL().remove(plans[i].getLabel());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            return body.detachApparatusByName(apparatusName);

        } catch (Exception ex) {

            throw JasonException.createWrongArgument(
                    this,
                    "Failed to detach apparatus at " + args[0]
            );
        }
    }

    private String getAppNameAsString() {

        if (this.appName == null) {
            return null;
        }

        return this.appName.toString();
    }

    private String getAppAddressAsString() {

        if (this.appAddress == null) {
            return null;
        }

        return neck.util.Util.getFormatedPortName(
                ((StringTerm) this.appAddress).getString()
        );
    }

    private static Body currentAgtBody(TransitionSystem ts) {

        AgArch arch = ts.getAgArch().getFirstAgArch();

        while (arch != null) {

            if (arch instanceof JaCaMoAgArch jcm) {
                return jcm.getAgtBody();
            }

            arch = arch.getNextAgArch();
        }

        throw new IllegalStateException(
                "JaCaMoAgArch não encontrado na cadeia de AgArch"
        );
    }
}
//package neck.internalActions;
//
//import jacamo.infra.JaCaMoAgArch;
//import jason.JasonException;
//import jason.architecture.AgArch;
//import jason.asSemantics.DefaultInternalAction;
//import jason.asSemantics.TransitionSystem;
//import jason.asSemantics.Unifier;
//import jason.asSyntax.Plan;
//import jason.asSyntax.Term;
//import neck.Body;
//
//public class DetachApparatus extends DefaultInternalAction {
//    private Term appName = null;
//
//    @Override
//    public int getMinArgs() {return 1;}
//
//    @Override
//    public int getMaxArgs() {return 1;}
//
//    @Override
//    protected void checkArguments(Term[] args) throws JasonException {
//        super.checkArguments(args); // check number of arguments
//        if (args.length == 1 && args[0].isAtom()){
//            this.appName = args[0];
//        }
//        else {
//            throw JasonException.createWrongArgument(this, "ERROR: Consult https://github.com/chon-group/JACAMOb/wiki");
//        }
//    }
//
//    @Override
//    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
//        /* EXPECTED
//            .myBody.detach(APPARATUSNAME)
//         */
//        checkArguments(args);
//        try {
//            if (currentAgtBody(ts) != null) {
//
//                Plan[] x = currentAgtBody(ts).getPlansByApparatusName(getAppNameAsString());
//
//                if (x != null) {
//                    for (int i = 0; i < x.length; i++) {
//                        System.out.println(x[i]);
//                        try {
//                            ts.getLogger().info("Removing from plan Library " + x[i].getLabel());
//                            ts.getAg().getPL().remove(x[i].getLabel());
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                }
//
//                return currentAgtBody(ts).detachApparatusByName(getAppNameAsString());
//            }
//            return false;
//        }catch (Exception ex) {
//            throw JasonException.createWrongArgument(
//                    this,
//                    "Failed to detach apparatus at " + getAppNameAsString()
//            );
//        }
//
//    }
//
//
//    private String getAppNameAsString(){
//        if(appName == null) return null;
//        return this.appName.toString();
//    }
//
//
//    private static Body currentAgtBody(TransitionSystem ts) {
//        AgArch arch = ts.getAgArch().getFirstAgArch();
//        while (arch != null) {
//            if (arch instanceof JaCaMoAgArch jcm) {
//                return jcm.getAgtBody(); // <- Body do agente atual
//            }
//            arch = arch.getNextAgArch();
//        }
//        throw new IllegalStateException("JaCaMoAgArch não encontrado na cadeia de AgArch");
//    }
//}
