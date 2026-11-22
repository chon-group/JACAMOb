counter(0).

!monitor.

+!monitor <-
    ?counter(N);
    M = N + 1;
    -+counter(M);
   // .print("My mind is alive. Counter=", M);
    .wait(1000);
    !monitor.

+default::bodyType(T)[source(percept),device(D)] <- .print("My body type: ", T, " Device: ", D).

-default::bodyType(T)[source(percept),device(D)] <- .print("Body perception removed: ", T, " Device: ", D).

