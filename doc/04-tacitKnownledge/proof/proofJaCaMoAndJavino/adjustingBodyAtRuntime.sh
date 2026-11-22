
(cd ../newBodyArtifact/ && jacamo app compile)
mv ../newBodyArtifact/build/classes/java/main/myBody/NewDuster.class build/classes/java/main/myBody/ -v

jacamo <<< 'agent run-as rosie { .remove_plan(p__7) }'

jacamo <<< 'agent run-as rosie {  makeArtifact("newDuster", "myBody.NewDuster", ["/dev/ttyUSB1"], Id); focus(Id); }'

jacamo <<< 'agent run-as rosie { .add_plan("+!sweep : not clean <- .print(sweepingWithNewArm); moveLeft; .wait(2000); moveRight; .wait(3000); !sweep.") }'
