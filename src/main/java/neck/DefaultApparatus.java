package neck;

import neck.util.SerialComm;
import org.json.JSONObject;

public class DefaultApparatus extends Apparatus {

    private SerialComm serialComm;

    public DefaultApparatus(String address) {
        super(address);
        this.serialComm = new SerialComm(address);
        this.serialComm.openConnection();
    }

    @Override
    public void act(String CMD) {

    }

    @Override
    public JSONObject perceive() {
        return serialComm.sendMsg("getPercepts");
    }

    @Override
    public JSONObject embody() {
        return null;
    }

}
