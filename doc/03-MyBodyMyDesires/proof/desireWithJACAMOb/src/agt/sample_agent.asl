!startClean.

+!startClean <- .print("Rosie started cleaning Elroy's bedroom."); +busy; !cleanBedroom.
+!cleanBedroom <- .wait(1000); !!cleanBedroom.
+!recharge <- .suspend(cleanBedroom); .stopMAS.

+!myBody::recharge(D)[element(powerSystem,battery)]: D = 1.0 <-
    ?myBody::batteryLevel(L);
    .print("Critical battery! Stopping cleaning. Drang=",D," Battery Level=",L);
    !recharge.

+!myBody::recharge(D)[element(powerSystem,battery)]: D < 1.0 & not busy <-
    ?myBody::batteryLevel(L);
    .print("Im not busy. Recharging... Drang=",D," Battery Level=",L);
    !recharge.

+!myBody::recharge(D)[element(powerSystem,battery)]: D < 1.0 & busy <-
    ?myBody::batteryLevel(L);
    .print("I need to recharge, but I'm busy cleaning. Drang=",D," Battery Level=",L);
    .wait(1000).
