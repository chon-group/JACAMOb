// Agent rosie
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

+!monitorBodyTemp <- 
    !decide; 
    .wait(500); 
    !monitorBodyTemp.

-!decide.

+status("burnt") <- .print("The robot burned out! temperature > 100ºC -- FAIL!!!"); .stopMAS.

/* new code */
+!togglePower <- embedded.mas.bridges.jacamo.defaultEmbeddedInternalAction("duster","togglePower",[]).
   
+!decide: default::temperature(T)[source(percept)] & T > 70 & default::powerStatus(P)[source(percept)] & P=="On" <- 
    .print("My mind: temperature(",T,") + powerStatus(",P,")"); !togglePower.

+!decide: default::temperature(T)[source(percept)] & T > 60 & default::powerStatus(P)[source(percept)] & P=="Off" <- 
    .print("Waiting for decreasing temperature... SUCCESS!"); .stopMAS.
    
+!decide: default::temperature(T)[source(percept)] & default::powerStatus(P)[source(percept)] <- 
    .print("My mind: temperature(",T,") + powerStatus(",P,")").
