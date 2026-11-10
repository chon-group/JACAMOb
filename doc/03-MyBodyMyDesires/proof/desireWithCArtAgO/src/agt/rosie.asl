!startClean.

+!startClean <- .print("Rosie started cleaning Elroy's bedroom."); +busy; !cleanBedroom.
+!cleanBedroom <- .wait(1000); !!cleanBedroom.
+!recharge <- .suspend(cleanBedroom); .stopMAS.

+recharge(D)[source(percept)]: D = 1.0 <-
    ?batteryLevel(L)[source(percept)];
    .print("Critical battery! Stopping cleaning. Drang=",D," Battery Level=",L);
    !recharge.

+recharge(D)[source(percept)]: D < 1.0 & not busy <-
    ?batteryLevel(L)[source(percept)];
    .print("Im not busy. Recharging... Drang=",D," Battery Level=",L);
    !recharge.

+recharge(D)[source(percept)]: D < 1.0 & busy <-
    ?batteryLevel(L)[source(percept)];
    .print("I need to recharge, but I'm busy cleaning. Drang=",D," Battery Level=",L);
    .wait(1000).

    
{ include("$jacamo/templates/common-cartago.asl") }