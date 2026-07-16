// Agent rosie in project jacamoProject
{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }

/* Initial beliefs and rules */

/* Plans */
+!saveBattery <- .wait(500); !tryRestAndRecharge.

+!tryRestAndRecharge[source(self)]: not busy<- .print("Done!"); .stopMAS.
-!tryRestAndRecharge <- !!saveBattery.

+!prepareCleaning[scheme(cleanHouse)] <- +busy; ?status(S); if (S == "on") { turnOff; }.
+!cleanLivingRoom[scheme(cleanHouse)] <- .random(R); .wait(10000*R); .print("Cleaning the living room.").
+!finishCleaning[scheme(cleanHouse)]  <- .print("Finishing the cleaning task."); ?status(S); if (S == "off") { turnOn; } -busy.