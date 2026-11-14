// Agent rosie in project proofJaCaMoAndJavino
//{ include("$jacamo/templates/common-cartago.asl") }

/* Initial goal */
//!embody.
//!cleanElroyBedroom.

+!embody <-
    .myBody.attach("/dev/ttyUSB0");
    .myBody.attach("/dev/ttyEmulatedPort0");
    !!cleanElroyBedroom.
/* Cleaning */
+!cleanElroyBedroom <- .print("Starting to clean Elroy's bedroom..."); !sweep.
+myBody::batteryLevel(L): L <= 5 <- .print("Critical battery level: ", L, "%"); !goToRecharge.
+myBody::batteryLevel(L)         <- .print("Battery Level=", L).

+!goToRecharge: not going <-
    +going;
    .print("GOING RECHARGING BASE");
    .intention(ThisID,_,_,current);
    .findall(Intention, .intend(Intention, IntentionID) & IntentionID \== ThisID, OtherIntentions);
    for (.member(I, OtherIntentions)) {
        .print("Suspending intention: ",I);
        .suspend(I);
    }
    .wait(myBody::batteryLevel(L) & L > 95);
    for (.member(I, OtherIntentions)) {
        .print("Resuming intention: ",I);
        .resume(I);
    } 
    -going;
.
+!goToRecharge: going.


//+!sweep : not clean <- .print("Sweeping..."); .myBody.act(togglePower); .wait(1000); !sweep.
+!sweep: clean.

-!G[error(action_failed), error_msg(M)] <-  .print("[ACTION FAILED] Plan: ",G, "  Msg:",M); .wait(3000); !G.

+myBody::port(off,Port,Apparatus,AptID)[source(interoception)] <-
    .print("Apparatus disconnected: ", Apparatus);
    .myBody.detach(Apparatus).

+serialEvent(add, Port)[source(neck)] <- .myBody.attach(Port).
//+serialEvent(remove, Port)[source(neck)] <- .myBody.detach(Port).