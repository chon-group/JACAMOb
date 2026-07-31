package jetsons.home;

import cartago.*;

public class PresenceSensor extends Artifact {

    void init() {
        defineObsProperty("humans", "yes");
    }
}