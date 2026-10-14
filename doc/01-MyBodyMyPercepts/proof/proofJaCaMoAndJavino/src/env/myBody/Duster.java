package myBody;

/*https://github.com/chon-group/Javino#how-to-import*/
import group.chon.javino.Javino; 
import cartago.*;

public class Duster extends Artifact {
    private Javino javino;
    private String serialPort = "";

    void init(String port) {
        defineObsProperty("temperature", 0.00);
        defineObsProperty("powerStatus", "unknown");
        initJavino(port);
        execInternalOp("getBodyInfo");
	}
    
    @Override
    public void dispose() {
        javino.closePort();
        super.dispose();
    }

    private void initJavino(String port) {
        this.serialPort = port;
        javino = new Javino();
        javino.infoPortStatus(false);
        javino.timeout(1000);
	}

    private String requesting(String DATA){
        if(javino.requestData(this.serialPort,DATA)) 
            return javino.getData();
        return null;
    }

    @OPERATION
	void togglePowerOperation() {
        javino.sendCommand(this.serialPort,"togglePower"); 
        System.out.println("[ARTIFACT] TOOGLEPOWER");
    }

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
                    if (t > 100){
                        defineObsProperty("status", "burnt"); 
                        System.out.println("[ARTIFACT] Burned!");
                        System.exit(0);
                    }
                    getObsProperty("temperature").updateValue(t);    
                }catch(Exception ex){
                    ex.printStackTrace();
                } 
            }         
            await_time(200);
        }    
    }
}