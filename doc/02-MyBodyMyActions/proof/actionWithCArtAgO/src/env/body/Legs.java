package body;

import java.util.logging.Logger;
import java.util.concurrent.Semaphore;
import cartago.*;
import group.chon.javino.Javino;

public class Legs extends Artifact {
    private final Semaphore serialSemaphore = new Semaphore(1, true);
    private Logger logger = Logger.getLogger("Artifact");
    private Javino javino;
    private String serialPort = "";

    void init(String port) {
        defineObsProperty("legState", "unknown");
        this.serialPort = port;
        javino = new Javino();
        javino.infoPortStatus(false);
        javino.timeout(5000);
        execInternalOp("getBodyInfo"); 
	}

    // SEM RESPOSTA, SOMENTE SIGNAL E FAILED
    @OPERATION
    void walk() {
        logger.info("keepWalking ...");
        String REPLY = requesting("KEEPWALKING");
        logger.info("keepWalking ..." + REPLY);
        if(REPLY != null && REPLY.equals("stuck")){
            //signal("unable"); 
            failed("unable");
        }
        else if (REPLY != null && REPLY.equals("ok")){
            signal("executed");
        }
        else if (REPLY != null && REPLY.equals("fall")){
            //signal("fall"); 
            failed("fall");
        }
    }
 	
    // COM RESPOSTA, SIGNAL E FAILED
    @OPERATION
    void standUp(OpFeedbackParam<String> feedback) {
        logger.info("standUP ...");
        String REPLY = requesting("STANDUP");
        logger.info("standUP ..." + REPLY);

        if (REPLY != null && REPLY.equals("stuck")) {
            feedback.set(REPLY);
            //failed("unable");
        }
        else if (REPLY != null && REPLY.equals("ok")) {
            feedback.set(REPLY);
        }
        else if (REPLY != null && REPLY.equals("fall")) {
            feedback.set(REPLY);
            //failed("fall");
        }
        else {
            failed("no_reply");
        }
    }
    @INTERNAL_OPERATION
    void getBodyInfo() throws Exception {
        ObsProperty prop = getObsProperty("legState");
        while(true){
            logger.info("Perceiving... ");  
            String DATA = requesting("LEGINFO");
            if(DATA != null && !DATA.equals(prop.stringValue())){
                getObsProperty("legState").updateValue(DATA);
                logger.info("legState="+DATA);    
            }
            logger.info("Perceived... ");  
            await_time(200);
        }    
    }
    

    private String requesting(String DATA){
        String out = null;
        try{
            serialSemaphore.acquire();
            /* using Javino in request mode */
            if(javino.requestData(this.serialPort,DATA)) out = javino.getData();     
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("Serial access interrupted: " + e.getMessage());
        }finally{
            serialSemaphore.release();
        }
        return out;   
    }
}