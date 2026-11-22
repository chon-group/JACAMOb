package myBody;

import group.chon.javino.Javino;
import cartago.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewDuster extends Artifact {

    private Javino javino;
    private String serialPort = "";
    private volatile boolean running = true;

    private static final Pattern PORT_PATTERN =
        Pattern.compile("port\\(([^,()]+),\\s*(on|off|timeout)\\);");

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
    void moveLeft() {
        if (!javino.sendCommand(serialPort, "MOVELEFT")) {
            failed("Could not communicate with new duster arm");
        }
    }

    @OPERATION
    void moveRight() {
        if (!javino.sendCommand(serialPort, "MOVERIGHT")) {
            failed("Could not communicate with new duster arm");
        }
    }

    private boolean isTimeout(String data) {
        return data != null && data.contains(",timeout);");
    }

    private void processPercepts(String data) {
        if (data == null) return;

        Matcher matcher = PORT_PATTERN.matcher(data);
        ObsProperty property = getObsProperty("serialPortStatus");

        while (matcher.find()) {
            String status = matcher.group(2);

            if (!status.equals(property.stringValue())) {
                property.updateValue(status);
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