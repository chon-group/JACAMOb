package neck;

import jason.asSyntax.ASSyntax;
import neck.model.SerialPortStatus;
import neck.util.SerialComm;
import org.json.JSONObject;

import java.util.logging.Logger;

public class DefaultApparatus extends Apparatus {
    Logger logger;
    private SerialComm serialComm;

    public DefaultApparatus(String address) {
        this.serialComm = new SerialComm(neck.util.Util.getFormatedPortName(address));
        super.setSerialComm(this.serialComm);
    }

    @Override
    public void act(String CMD) {

    }

    @Override
    public JSONObject perceive() {
        JSONObject out = serialComm.sendMsg("getPercepts");
        //System.out.println(out.toString());
        return out;
    }

    @Override
    public JSONObject embody() {
        return null;
    }

}