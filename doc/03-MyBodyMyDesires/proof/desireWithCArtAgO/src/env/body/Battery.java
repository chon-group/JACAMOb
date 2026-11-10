
package body;

import java.util.logging.Logger;
import java.util.concurrent.Semaphore;

import cartago.*;
import group.chon.javino.Javino;

public class Battery extends Artifact {

    private static final double BATTERY_LOW = 15.0;
    private static final double BATTERY_CRITICAL = 5.0;

    private final Semaphore serialSemaphore = new Semaphore(1, true);
    private final Logger logger = Logger.getLogger("Artifact");

    private Javino javino;
    private String serialPort = "";

    void init(String port) {
        defineObsProperty("batteryLevel", 100.0);

        this.serialPort = port;

        javino = new Javino();
        javino.infoPortStatus(false);
        javino.timeout(5000);

        execInternalOp("getBodyInfo");
    }

    @INTERNAL_OPERATION
    void getBodyInfo() throws Exception {

        ObsProperty prop = getObsProperty("batteryLevel");

        while (true) {

            logger.info("Perceiving...");

            String data = requesting("BATTERY");

            if (data != null) {
                try {
                    double level = Double.parseDouble(data.trim());

                    if (!Double.isFinite(level) || level < 0.0 || level > 100.0) {
                        logger.warning("Invalid battery level: " + data);
                    } else if (Double.compare(level, prop.doubleValue()) != 0) {
                        prop.updateValue(level);
                        logger.info("batteryLevel=" + level);

                        double drang = calculateDrang(level);

                        if (drang > 0.0) {
                            signal("recharge", drang);
                            logger.info("recharge(" + drang + ")");
                        }
                    }

                } catch (NumberFormatException e) {
                    logger.warning("Invalid battery level: " + data);
                }
            }

            await_time(200);
        }
    }

    private double calculateDrang(double level) {
        if (level > BATTERY_LOW) return 0.0;

        // Drang starts at 0.1 at 15% and reaches 1.0 at 5%.
        double drang = 0.1 +
                0.9 * (BATTERY_LOW - level) /
                (BATTERY_LOW - BATTERY_CRITICAL);

        return Math.max(0.1, Math.min(1.0, drang));
    }

    private String requesting(String data) {

        String out = null;
        boolean acquired = false;

        try {
            serialSemaphore.acquire();
            acquired = true;

            if (javino.requestData(this.serialPort, data)) {
                out = javino.getData();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning("Serial access interrupted: " + e.getMessage());

        } finally {
            if (acquired) {
                serialSemaphore.release();
            }
        }

        return out;
    }
}
