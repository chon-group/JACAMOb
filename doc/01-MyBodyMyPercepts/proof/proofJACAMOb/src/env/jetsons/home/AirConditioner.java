// CArtAgO artifact code for project jacamoProject

package jetsons.home;

import cartago.*;

public class AirConditioner extends Artifact {

    void init() {
       // neck.util.Trace.log("INICIOU O ARTEFATO EXEMPLO");
        defineObsProperty("status", "On");
        defineObsProperty("temperature", 22);
    }

    @OPERATION
    void turnOn() {
		System.out.println("[ARTIFACT] AirConditioner is TURING OFF!");
        getObsProperty("status").updateValue("on");
    }

    @OPERATION
    void turnOff() {
		System.out.println("[ARTIFACT] AirConditioner is TURING OFF!");
        getObsProperty("status").updateValue("off");
    }
}