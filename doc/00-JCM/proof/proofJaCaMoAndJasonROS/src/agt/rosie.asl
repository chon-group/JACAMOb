// Agent rosie in project proofJACAMOb
{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }


/* Plans */
+!helloWorld <- .print("hello world."); .wait(5000); .stopMAS.
