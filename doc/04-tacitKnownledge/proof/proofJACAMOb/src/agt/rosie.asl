!cleanElroyBedroom.

+!cleanElroyBedroom <- .print("Cleaning Elroy's bedroom..."); !sweep; makeTheBed; gatherDirtyClothes.

+!sweep: clean <- .print("Sweep task finished!").
+!makeTheBed: bedOk <- .print("Make the bed task finished!").
+!gatherDirtyClothes: withoutDirtyClothes <- .print("Gather dirty clothes task finished!").

+!goToRecharge: recharging.

+!goToRecharge: not recharging <-
    +recharging; -recharged; .print("GOING TO RECHARGING BASE");
    .intention(ThisID,_,_,current);
    .findall(Intention, .intend(Intention, IntentionID) & IntentionID \== ThisID, OtherIntentions);
    for (.member(I, OtherIntentions)) {.print("Suspending intention: ",I); .suspend(I);}
    .wait(recharged[source(self)]);
    for (.member(I, OtherIntentions)) {.print("Resuming intention: ",I); .resume(I);} 
    -recharging.


+myBody::batteryLevel(L): L <= 5  <- .print("Critical battery level: ", L, "%"); !goToRecharge.
+myBody::batteryLevel(L): L >  95 <- .print("Battery Level=", L); +recharged.

+myBody::port(off,Port,Apparatus,AptID)[source(interoception)] <-
    .print("Apparatus disconnected: ", Apparatus);
    .myBody.detach(Apparatus).

+serialEvent(add, Port)[source(neck)] <- .myBody.attach(Port).