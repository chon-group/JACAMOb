#include "common.h"
#include <NECK.hpp>

Apparatus(powerSystem) {Element(battery);}

Preparation{configBody();}

Sensing(battery) {updateBattery();}

Percept(battery, batteryLevel, INTEROCEPTION) {
    return getBatteryLevel();
}

Trieb(battery, recharge) {
    float level = getBatteryLevel();

    if (level > BATTERY_LOW) return 0.0f;
    
    // Drang começa em 0.1 aos 15% e atinge 1.0 aos 5%.
    float drang = 0.1f +
        0.9f * (BATTERY_LOW - level) /
        (BATTERY_LOW - BATTERY_CRITICAL);

    return constrain(drang, 0.1f, 1.0f);
}
