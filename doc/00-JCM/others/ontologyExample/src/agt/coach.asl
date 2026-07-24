// uncomment the include below to have an agent compliant with its organisation
//{ include("$moise/asl/org-obedient.asl") }

{ include("$jacamo/templates/common-cartago.asl") }
{ include("$jacamo/templates/common-moise.asl") }

!start.


+!start <-
    isInstanceOf("john", "RightMidfielder", R1);
    .print("john is RightMidfielder? ", R1);

    isInstanceOf("john", "Midfielder", R2);
    .print("john is Midfielder? ", R2);

    isInstanceOf("john", "Player", R3);
    .print("john is Player? ", R3);

    isInstanceOf("john", "Goalkeeper", R4);
    .print("john is Goalkeeper? ", R4);

    .stopMAS;
.


