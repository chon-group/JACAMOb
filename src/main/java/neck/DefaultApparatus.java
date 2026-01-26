package neck;

import neck.model.SerialPortStatus;
import neck.util.SerialComm;
import org.json.JSONObject;

import java.util.logging.Logger;

public class DefaultApparatus extends Apparatus {
    Logger logger;
    private SerialComm serialComm;

    public DefaultApparatus(String address) {
        super(address);
        this.logger = Logger.getLogger(address);
        this.serialComm = new SerialComm(address);
        this.serialComm.openConnection();
        if(this.serialComm.getPortStatus() == SerialPortStatus.ON){
            this.setStatus(true);
        }else{
            this.setStatus(false);
        }
    }

    @Override
    public void act(String CMD) {

    }

    @Override
    public JSONObject perceive() {
        JSONObject out =serialComm.sendMsg("getPercepts");
        //System.out.println(out.toString());
        return out;
    }

    @Override
    public JSONObject embody() {
        return null;
    }

}
