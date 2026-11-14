// Agent rosie in project proofJaCaMoAndJavino
{ include("$jacamo/templates/common-cartago.asl") }

/* Initial goal */
!cleanElroyBedroom.

/* Cleaning */
+!cleanElroyBedroom <- .print("Starting to clean Elroy's bedroom..."); !sweep.
+batteryLevel(L): L <= 5 <- .print("Critical battery level: ", L, "%"); !goToRecharge.
+batteryLevel(L)         <- .print("Battery Level=", L).

+!goToRecharge: not going <-
    +going;
    .print("GOING RECHARGING BASE");
    .intention(ThisID,_,_,current);
    .findall(Intention, .intend(Intention, IntentionID) & IntentionID \== ThisID, OtherIntentions);
    for (.member(I, OtherIntentions)) {
        .print("Suspending intention: ",I);
        .suspend(I);
    }
    .wait(batteryLevel(L) & L > 95);
    for (.member(I, OtherIntentions)) {
        .print("Resuming intention: ",I);
        .resume(I);
    } 
    -going;
.
+!goToRecharge: going.


+!sweep : not clean <- .print("Sweeping..."); togglePowerOperation; .wait(1000); !sweep.
+!sweep: clean.

-!G[error(action_failed), error_msg(M)] <-  .print("[ACTION FAILED] Plan: ",G, "  Msg:",M); .wait(3000); !G.

+serialPortStatus("off")[artifact_id(Id), artifact_name(Artifact)] <-
    .print("Artifact disconnected: ", Artifact);
    stopFocus(Id);
    disposeArtifact(Id).