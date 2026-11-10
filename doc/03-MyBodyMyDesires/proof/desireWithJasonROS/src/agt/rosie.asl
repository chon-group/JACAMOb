!startClean.

+!startClean <- .print("Rosie started cleaning Elroy's bedroom."); +busy; !cleanBedroom.
+!cleanBedroom <- .wait(1000); !!cleanBedroom.
+!recharge <- .suspend(cleanBedroom); .stopMAS.

+batteryLevel(L)[device(arduino1),source(percept)]: L < 5 <- .print("Stopping cleaning. Battery Level=",L); !recharge.

+batteryLevel(L)[device(arduino1),source(percept)]: L < 15 & not busy <- .print("Im not busy. Battery Level=",L); !recharge.

+batteryLevel(L)[device(arduino1),source(percept)]: L < 15 & busy <- .print("I'm busy. Battery Level=",L); .wait(1000).