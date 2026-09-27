!testingBroadcast.

+!testingBroadcast <- .wait(40000); 
    .print("Sending a broadcast, about my mind");
    .broadcast(tell,outro::temperature(20)[device(duster)]);
    .broadcast(tell,outro::powerStatus("On")[device(duster)]).