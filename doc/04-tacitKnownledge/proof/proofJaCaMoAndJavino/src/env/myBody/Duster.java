package myBody;

import group.chon.javino.Javino;
import cartago.*;

public class Duster extends Artifact {

    private Javino javino;
    private String serialPort = "";
    private volatile boolean running = true;

    void init(String port) {
        defineObsProperty("serialPortStatus", "unknown");
        initJavino(port);
        execInternalOp("getBodyInfo");
    }

    @Override
    public void dispose() {
        running = false;

        if (javino != null) {
            javino.closePort();
        }

        super.dispose();
    }

    private void initJavino(String port) {
        serialPort = port;
        javino = new Javino();
        javino.infoPortStatus(true);
        javino.timeout(3000);
    }

    private String requesting(String data) {
        try {
            javino.requestData(serialPort, data);
            return javino.getData();
        } catch (Exception ex) {
            System.out.println(
                "[JAVINO] Communication failure: " + ex.getMessage()
            );
        }

        return null;
    }

    @OPERATION
    void togglePowerOperation() {
        if (!javino.sendCommand(serialPort, "TOGGLEPOWER")) {
            failed("Could not communicate with duster arm");
        }
    }

    private boolean isTimeout(String data) {
        return data != null && data.contains(",timeout);");
    }

    private void processPercepts(String data) {
        if (data == null) return;

        String[] percepts = data.split(";");

        for (String percept : percepts) {
            percept = percept.trim();

            if (percept.startsWith("port(")
                    && percept.endsWith(")")) {

                String value = percept.substring(
                    "port(".length(),
                    percept.length() - 1
                );

                String[] args = value.split(",", 2);

                if (args.length == 2) {
                    String status = args[1].trim();

                    ObsProperty property =
                        getObsProperty("serialPortStatus");

                    if (!status.equals(property.stringValue())) {
                        property.updateValue(status);
                    }
                }
            }
        }
    }

    @INTERNAL_OPERATION
    void getBodyInfo() throws Exception {
        System.out.println("[JAVINO] Starting: " + serialPort);

        while (running) {

            String data = requesting("getPercepts");
            if (!running) break;

            processPercepts(data);

            if (isTimeout(data)) {
                System.out.println(
                    "[JAVINO] Timeout. Reconnecting..."
                );

                javino.closePort();

                if (!running) break;

                data = requesting("getPercepts");

                if (!running) break;

                processPercepts(data);
            }

            await_time(200);
        }
    }
}