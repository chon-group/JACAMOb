package myBody;

import cartago.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import group.chon.javino.Javino;


public class Duster extends Artifact {
    private Javino javino;
    private String serialPort = "";

    void init(String port) {
        defineObsProperty("temperature", 0.00);
        defineObsProperty("powerStatus", "unknown");
        this.serialPort = port;
        javino = new Javino();
        javino.infoPortStatus(false);
        javino.timeout(1000);
        execInternalOp("getBodyInfo");
	}

	@OPERATION
	void togglePower() {javino.sendCommand(this.serialPort,"togglePower"); System.out.println("[ARTIFACT] TOOGLEPOWER");}

 	@INTERNAL_OPERATION
    void getBodyInfo() throws Exception {
        System.out.println("[JAVINO] Starting: "+serialPort);
        while(true){
            String DATA = requesting("POWERINFO");
            
            if(DATA != null){
                getObsProperty("powerStatus").updateValue(DATA);
            }
            
            DATA = requesting("TEMPINFO");
            if (DATA != null) {
                try{
                    double t = Double.parseDouble(DATA);
                    if (t > 100){defineObsProperty("status", "burnt"); System.out.println("[ARTIFACT] The robot burned out! temperature > 100ºC -- FAIL!!!");}
                    getObsProperty("temperature").updateValue(t);    
                }catch(Exception ex){

                } 
            }         
            await_time(200);
        }    
    }

	
    @Override
    public void dispose() {
        javino.closePort();
        super.dispose();
    }


    private String requesting(String DATA){
        if(javino.requestData(this.serialPort,DATA)){
            return javino.getData();
        }
        return null;
    }
}

