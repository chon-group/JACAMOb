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
    .wait(2000);
    !!monitorBodyTemp.

+!monitorBodyTemp <-  !decide; .wait(250); !monitorBodyTemp.

+status("burnt") <- .print("The robot burned out! temperature > 100ºC -- FAIL!!!"); .stopMAS.

/* new code */
+!togglePower <- ext::togglePower.
  
+!decide: default::temperature(T)[source(percept)] & default::powerStatus(P)[source(percept)] 
& P=="On" & T > 70 <- 
    .print("My mind: temperature(",T,") + powerStatus(",P,")"); -+current(T); !togglePower.

+!decide: default::temperature(T)[source(percept)] & default::powerStatus(P)[source(percept)] 
& current(C)[source(self)] & P=="Off" & T < C <- 
    .print("Waiting for decreasing temperature... SUCCESS!"); .stopMAS.
    
+!decide: default::temperature(T)[source(percept)] & default::powerStatus(P)[source(percept)] <- 
    .print("My mind: temperature(",T,") + powerStatus(",P,")"); -+current(T).
