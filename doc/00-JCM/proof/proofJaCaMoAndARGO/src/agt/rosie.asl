// Agent rosie in project proofJACAMOb
{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }

/* Initial Intention */
!connect.

+!connect <- 
    .argo.port("/dev/ttyEmulatedPort0"); 
    .argo.port("/dev/ttyEmulatedPort1"); 
    .argo.percepts(open).

/* Plans */
+!helloWorld <- .print("hello world."); .wait(5000); .stopMAS.
