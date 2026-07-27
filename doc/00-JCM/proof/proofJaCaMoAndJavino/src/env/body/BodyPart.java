package body;

import cartago.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import group.chon.javino.Javino;


public class BodyPart extends Artifact {
    private Javino javino;
    private String serialPort = "";

    void init(String port) {
        this.serialPort = port;
        javino = new Javino();
        javino.infoPortStatus(false);
        javino.timeout(100);
        execInternalOp("getBodyInfo");
	}

 	@INTERNAL_OPERATION
    void getBodyInfo() throws Exception {
        System.out.println("[JAVINO] Starting: "+serialPort);
        while(true){
            String DATA = requesting("getPercepts");
            await_time(100);
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

