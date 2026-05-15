steps(0).

!embody.

+!embody <- 
    .print("Starting ACTION Experiment wit JACAMOb");
    .myBody.neckAttach(walker,"/dev/ttyEmulatedPort0").

+!walk: steps(N) & N >= 10 <-
    .print("I arrived at my destination without falling....");
    .stopMAS.

+!walk: myBody::legState(ok)<-
    .myBody.act(keepWalking,Reply);
    !actionReply(Reply);
    !walk.

-!walk <- .print("I'm not able to walk !!! [not (myBody::legState(ok))]").

/* bodyResponse of ACTION execution */
+!actionReply(unable) <- 
    .drop_intention(walk);
    .print("The action returned UNABLE [.drop_intention(walk)]...").

+!actionReply(executed): steps(N) <- -+steps(N+1); .print("Steps so far: ",N+1).

/* Beliefs Plans */
+myBody::legState(ok)     <-
    .print("I perceived that my leg is OK! Keep Walking...");
    !walk.

+myBody::legState(stuck)  <-
    .drop_desire(walk);
    .print("I perceived that my leg is stuck [-!walk]...").

+myBody::legState(fall)   <-
    .print("I perceived that I fell (.stopMAS)...");
    .stopMAS.