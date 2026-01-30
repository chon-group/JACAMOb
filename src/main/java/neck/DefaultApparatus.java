package neck;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Term;
import neck.model.BodyResponse;
import neck.model.SerialPortStatus;
import neck.util.SerialComm;
import org.json.JSONObject;

import java.util.Objects;
import java.util.logging.Logger;

public class DefaultApparatus extends Apparatus {
    Logger logger;
    //private SerialComm serialComm;

    public DefaultApparatus(String address) {
        super(new SerialComm(neck.util.Util.getFormatedPortName(address)));
    }

    @Override
    public BodyResponse act(Term actionTerm) {
        return BodyResponse.jsonObjectToBodyResponse(
                super.getSerialComm().sendMsg(
                        neck.util.Util.getFunctor(actionTerm),
                        neck.util.Util.argsOfTermToObjects(actionTerm)
                )
        );
    }

    @Override
    public JSONObject perceive() {
        JSONObject out = super.getSerialComm().sendMsg("getPercepts");
        //System.out.println(out.toString());
        return out;
    }

    @Override
    public JSONObject embody() {
        return null;
    }

}