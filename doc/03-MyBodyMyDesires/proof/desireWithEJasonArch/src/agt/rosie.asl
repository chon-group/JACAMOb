!startClean.

+!startClean <- .print("Rosie started cleaning Elroy's bedroom."); +busy; !cleanBedroom.
+!cleanBedroom <- .wait(1000); !!cleanBedroom.
+!recharge <- .suspend(cleanBedroom); .stopMAS.

+batteryLevel(L)[source(percept)]: L < 5 <- .print("Critical battery! Stopping cleaning. Battery Level=",L); !recharge.

+batteryLevel(L)[source(percept)]: L < 15 & not busy <- .print("Im not busy. Recharging... Battery Level=",L); !recharge.

+batteryLevel(L)[source(percept)]: L < 15 & busy <- .print("I need to recharge, but I'm busy cleaning. Battery Level=",L); .wait(1000).