counter(0).

!monitor.

/* Internal activity */

+!monitor <-
    ?counter(N);
    -+counter(N+1);
    .print("My mind is alive. Counter=", N+1);
    .wait(1000);
    !monitor.

/* Body perceptions */

+bodyType(T)[source(percept)] <- .print("My body type: ", T).

-bodyType(T)[source(percept)] <- .print("Body perception removed: ", T).