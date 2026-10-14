// Agent rosie in project proofJaCaMoAndJavino
{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }
{ include("$moise/asl/org-obedient.asl") }

/* Plans */

+!cleanLivingRoom[scheme(cleaning)] <- 
    .print("Starting to clean the living room..."); 
    !startWork.

+!startWork <- 
    !togglePower; 
    !!monitorBodyTemp.

+!monitorBodyTemp <- !decide; .wait(250); !monitorBodyTemp.


+status("burnt") <- .print("The robot burned out! temperature > 100ºC -- FAIL!!!"); .stopMAS.

/* new code */
+!decide: body::temperature(T)[source(percept),artifact_name(duster)] 
& body::powerStatus(P)[source(percept),artifact_name(duster)] & P=="On" & T > 70 <- 
    .print("Mind: temp(",T,") + powerStatus(",P,")"); 
    -+current(T);
    !togglePower;
    .wait(500).

+!decide: body::temperature(T)[source(percept),artifact_name(duster)] 
& body::powerStatus(P)[source(percept),artifact_name(duster)] & current(C)[source(self)] & P=="Off" & T < C <- 
 .print("Decreasing temp(",T,")... SUCCESS!");
 .wait(10000);
 .stopMAS.
    
+!decide: body::temperature(T)[source(percept),artifact_name(duster)] 
& body::powerStatus(P)[source(percept),artifact_name(duster)] <- 
    .print("Mind: temp(",T,") + powerStatus(",P,")"); 
    -+current(T);
    .wait(500).
    
+!togglePower <- togglePowerOperation.