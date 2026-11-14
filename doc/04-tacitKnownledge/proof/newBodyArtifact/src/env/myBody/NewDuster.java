
package myBody;

import group.chon.javino.Javino;
import cartago.*;

public class NewDuster extends Artifact {

    private Javino javino;
    private String serialPort = "";
    private volatile boolean running = true;

    void init(String port) {
        defineObsProperty("temperature", 0.00);
        defineObsProperty("direction", "unknown");
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
        javino.timeout(1000);
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
    void turnLeftArm() {
        if (!javino.sendCommand(serialPort, "left")) {
            failed("Could not communicate with new duster arm");
        }
        //System.out.println("[ARTIFACT] TURN LEFT ARM");
    }

    @OPERATION
    void turnRightArm() {
        if (!javino.sendCommand(serialPort, "right")) {
            failed("Could not communicate with new duster arm");
        }
        //System.out.println("[ARTIFACT] TURN RIGHT ARM");
    }

    @OPERATION
    void stopArm() {
        if (!javino.sendCommand(serialPort, "stop")) {
            failed("Could not communicate with new duster arm");
        }
        //System.out.println("[ARTIFACT] STOP ARM");
    }

    private boolean isTimeout(String data) {
        return data != null && data.contains(",timeout);");
    }

    private void processPercepts(String data) {
        if (data == null) return;

        String[] percepts = data.split(";");

        for (String percept : percepts) {
            percept = percept.trim();

            if (percept.startsWith("direction(")
                    && percept.endsWith(")")) {

                String value = percept.substring(
                    "direction(".length(),
                    percept.length() - 1
                );

                getObsProperty("direction").updateValue(value);

            } else if (percept.startsWith("temperature(")
                    && percept.endsWith(")")) {

                String value = percept.substring(
                    "temperature(".length(),
                    percept.length() - 1
                );

                try {
                    getObsProperty("temperature").updateValue(
                        Double.parseDouble(value)
                    );
                } catch (NumberFormatException ex) {
                    System.out.println(
                        "[ARTIFACT] Invalid temperature: " + value
                    );
                }

            } else if (percept.startsWith("port(")
                    && percept.endsWith(")")) {

                String value = percept.substring(
                    "port(".length(),
                    percept.length() - 1
                );

                String[] args = value.split(",", 2);

                if (args.length == 2) {
                    getObsProperty("serialPortStatus")
                        .updateValue(args[1].trim());
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

            if (isTimeout(data)) {
                System.out.println(
                    "[JAVINO] Timeout. Reconnecting..."
                );

                javino.closePort();

                if (!running) break;

                data = requesting("getPercepts");
            }

            if (!running) break;

            processPercepts(data);

            await_time(200);
        }
    }
}
