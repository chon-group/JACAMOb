#include "batteryModule.h"
#include <NECK.hpp>

Apparatus(powerModule) {Element(batteryCell);}

Preparation {PowerModule::setupBattery();}

Sensing(batteryCell) {PowerModule::updateBattery();}

Percept(batteryCell, batteryLevel, INTEROCEPTION) {
    return (int32_t) PowerModule::getBatteryLevel();
}
