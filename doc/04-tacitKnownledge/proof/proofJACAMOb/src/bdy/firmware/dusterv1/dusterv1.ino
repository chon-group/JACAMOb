#include "armModelA.h"
#include <NECK.hpp>

Apparatus(duster) {Element(broom);}

Preparation {setupArm();}

Act(broom, togglePower) {togglePower(); return EXECUTED;}

TacitKnowledge(sweep,"not clean",".print(sweeping); .myBody.act(togglePower); .wait(1000); !sweep.");
