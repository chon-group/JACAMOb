package myBody;

import group.chon.javino.Javino;
import cartago.*;

public class Battery extends Artifact {
    private Javino javino;
    private String serialPort = "";
    private volatile boolean running = true;

    void init(String port) {
        defineObsProperty("batteryLevel", 100);
        initJavino(port);
        execInternalOp("getBatteryInfo");
    }

    @Override
    public void dispose() {
        running = false;
        if (javino != null) javino.closePort();
        super.dispose();
    }

    private void initJavino(String port) {
        serialPort = port;
        javino = new Javino();
        javino.infoPortStatus(false);
        javino.timeout(1000);
    }

    private String requesting(String data) {
        try {
            if (javino.requestData(serialPort, data)) {
                return javino.getData();
            }
        } catch (Exception ex) {
            System.out.println("[BATTERY] Communication failure: " + ex.getMessage());
        }
        return null;
    }

   @INTERNAL_OPERATION
    void getBatteryInfo() throws Exception {
        System.out.println("[BATTERY] Monitoring: " + serialPort);

        while (running) {
            String data = requesting("BATTERYINFO");
            if (!running) break;

            if (data != null) {
                try {
                    int level = Integer.parseInt(data.trim());

                    if (level >= 0 && level <= 100) {
                        ObsProperty battery = getObsProperty("batteryLevel");

                        if (battery.intValue() != level) {
                            battery.updateValue(level);
                        }
                    }

                } catch (NumberFormatException ex) {
                    System.out.println("[BATTERY] Invalid battery level: " + data);
                }
            }

            await_time(200);
        }
    }
}