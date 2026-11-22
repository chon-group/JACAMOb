#include "armModelB.h"
#include <NECK.hpp>

Apparatus(dusterv2) {Element(broom);}

Preparation {setupArm();}

Act(broom, moveLeft) {
    moveLeft();
    return EXECUTED;
}

Act(broom, moveRight) {
    moveRight();
    return EXECUTED;
}


TacitKnowledge(sweep,
    "not clean",
    ".print(sweeping); .myBody.act(moveLeft); .myBody.act(moveRight); .wait(1000); !sweep.");
