// CArtAgO artifact code for project jacamoProject

package jetsons.home;

import cartago.*;

public class AirConditioner extends Artifact {

    void init() {
       // neck.util.Trace.log("INICIOU O ARTEFATO EXEMPLO");
        defineObsProperty("powerStatus", "On");
        defineObsProperty("temperature", 22);
    }

    @OPERATION
    void turnOn() {
		System.out.println("[ARTIFACT] AirConditioner is TURING OFF!");
        getObsProperty("powerStatus").updateValue("On");
    }

    @OPERATION
    void turnOff() {
		System.out.println("[ARTIFACT] AirConditioner is TURING OFF!");
        getObsProperty("powerStatus").updateValue("Off");
    }
}