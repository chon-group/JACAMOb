// CArtAgO artifact code for project jacamoProject

package jetsons.home;

import cartago.*;

public class Light extends Artifact {

    void init() {
     //   neck.util.Trace.log("INICIOU O ARTEFATO EXEMPLO");
        defineObsProperty("light", "on");
    }

    @OPERATION
    void turnOn() {
		System.out.println("[ARTIFACT] Light is TURING OFF!");
        getObsProperty("status").updateValue("on");
    }

    @OPERATION
    void turnOff() {
		System.out.println("[ARTIFACT] Light is TURING OFF!");
        getObsProperty("status").updateValue("off");
    }
}