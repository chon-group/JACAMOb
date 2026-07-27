// Agent rosie in project jacamoProject
{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }

/* Initial beliefs and rules */

/* Plans */
+!saveBattery <- .random(R); .wait(5000*R); !!tryRestAndRecharge.
+!tryRestAndRecharge[source(self)]: not busy <- .print("Done!"); .stopMAS.
-!tryRestAndRecharge <- !!saveBattery.

+!prepareCleaning[scheme(cleanHouse)] <- +busy; ?status(S); if (S == "on") { turnOff; }.
+!cleanLivingRoom[scheme(cleanHouse)] <- .print("Cleaning the living room."); .random(R); .wait(10000*R).
+!finishCleaning[scheme(cleanHouse)]  <- ?status(S); if (S == "off") { turnOn; } -busy.


+constitutive_rule(rosie, robotic_worker, true, true)<-
     .print(">>> INSTITUIÇÃO: Perante a instituição, eu sou uma trabalhadora robótica!").


//!joinOrganisation.
// +!joinOrganisation
// <-
//     joinWorkspace(jetsonsFamily, WspId);
//     lookupArtifact(householdStaff, GrArtId)[wid(WspId)];
//     adoptRole(housekeeper)[artifact_id(GrArtId)];
//     .print("Rosie assumiu o papel housekeeper.");

// +!joinOrganisation
// <-
//     joinWorkspace(jetsonsFamily, WspId);

//     // OrgBoard
//     lookupArtifact(jetsonsFamily, OrgArtId)[wid(WspId)];
//     focus(OrgArtId)[wid(WspId)];

//     // GroupBoard
//     lookupArtifact(householdStaff, GrArtId)[wid(WspId)];
//     focus(GrArtId)[wid(WspId)];

//     // Assume o papel
//     adoptRole(housekeeper)[artifact_id(GrArtId)];

//     .print("Rosie assumiu o papel housekeeper.").


!blink.

+!blink <-  .myBody.act(blinkOperation(true),Reply); .print("Action:",Reply).
