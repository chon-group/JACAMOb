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
    .wait(250); 
    !monitorBodyTemp.

+status("burnt") <- .print("The robot burned out! temperature > 100ºC -- FAIL!!!"); .stopMAS.

/* new code */ 
+!decide: default::temperature(T)[source(percept),device(duster)] & default::powerStatus(P)[source(percept),device(duster)] 
& P=="On" & T > 70 <- 
    .print("My mind: temperature(",T,") + powerStatus(",P,") \n togglePower"); !togglePower; -+current(T).

+!decide: default::temperature(T)[source(percept),device(duster)] & default::powerStatus(P)[source(percept),device(duster)] 
& current(C)[source(self)] & P=="Off" & T < C <- 
    .print("Waiting for decreasing temperature... SUCCESS!"); .stopMAS.
    
+!decide: default::temperature(T)[source(percept),device(duster)] & default::powerStatus(P)[source(percept),device(duster)] <- 
    .print("My mind: temperature(",T,") + powerStatus(",P,")"); -+current(T).

+!togglePower <- embedded.mas.bridges.jacamo.defaultEmbeddedInternalAction("duster","togglePower",[]).