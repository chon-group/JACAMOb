steps(5).
!embody.
+!embody <- .myBody.neckAttach(walker,"/dev/ttyEmulatedPort0").

+!walk: steps(N) & N < 1 <-
    .print("I arrived at my destination without falling....");
    .stopMAS.

+!walk: myBody::legState(ok) <-
    ?steps(N);
    .print("Steps so far: ",N);
    .myBody.act(keepWalking,Reply);
    !actionReply(Reply);
    .wait(myBody::legState(ok));
    !walk.

-!walk <- .print("I'm not able to walk !!!").

/* replyAction action execution */
+!actionReply(unable) <- .print("The action returned UNABLE (.drop_intention(walk))..."); .drop_intention(walk).
+!actionReply(executed) <- ?steps(N); -+steps(N-1).

/* Beliefs Plans */

+myBody::legState(ok)     <-
    .print("I perceived that my leg is OK! Keep Walking...");
    !walk.

+myBody::legState(stuck)  <-
    .print("I perceived that my leg is stuck (-!walk)...");
    .drop_desire(walk).

+myBody::legState(fall)   <-
    .print("I perceived that I fell (.stopMAS)...");
    .stopMAS.