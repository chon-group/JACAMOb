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

/* addition */

+!sweep : not clean <- .print("Sweeping..."); togglePowerOperation; .wait(3500); !sweep.
-!G[error(action_failed), error_msg(M)] <-  .print("[ACTION FAILED] Plan: ",G, "  Msg:",M); .wait(3000); !G.

+batteryLevel(L): L <= 5 <- .print("Critical  battery level: ", L, "%"); !goToRecharge.
+batteryLevel(L): L > 95 <- .print("Recharged battery level: ", L, "%"); +recharged.

+serialPortStatus("off")[artifact_id(Id), artifact_name(Artifact)] <-
    .print("Artifact disconnected: ", Artifact);
    stopFocus(Id);
    disposeArtifact(Id).

{ include("$jacamo/templates/common-cartago.asl") }