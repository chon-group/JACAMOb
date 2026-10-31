package body;
import java.util.logging.Logger;
import java.util.concurrent.Semaphore;
import cartago.*;
import group.chon.javino.Javino; /*https://github.com/chon-group/Javino#how-to-import*/

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

 @OPERATION
 void walk() {
  logger.info("keepWalking ...");
  String R = requesting("KEEPWALKING");
  logger.info("keepWalking ..." + R);
  if(R != null && R.equals("stuck")){failed("unable");}
  else if (R != null && R.equals("ok")){signal("executed");}
  else if (R != null && R.equals("fall")){failed("fall");}
 }

 @OPERATION
 void standUp(OpFeedbackParam<String> feedback) {
  logger.info("standUP ...");
  String R = requesting("STANDUP");
  logger.info("standUP ..." + R);
  if (R != null && R.equals("stuck")) {feedback.set(R);}
  else if (R != null && R.equals("ok")) {feedback.set(R);}
  else if (R != null && R.equals("fall")) {feedback.set(R);}
  else {failed("no_R");}
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
   if(javino.requestData(this.serialPort,DATA)){
    out = javino.getData();     
   } 
  }catch (InterruptedException e) {
   Thread.currentThread().interrupt();
   logger.warning("Serial access interrupted: " + e.getMessage());
  }finally{
   serialSemaphore.release();
  }
  return out;   
 }
}