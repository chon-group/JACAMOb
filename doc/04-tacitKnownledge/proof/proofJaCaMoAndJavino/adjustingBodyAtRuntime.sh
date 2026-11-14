#unzip -o runtimeClasses/myBody.zip -d build/classes/java/main/
#jason agent load-into --source=runtimeClasses/myBody.asl rosie

jacamo <<< 'agent run-as rosie { .remove_plan(p__11) }'

(cd ../newBodyArtifact/ && jacamo app compile)
mv ../newBodyArtifact/build/classes/java/main/myBody/NewDuster.class build/classes/java/main/myBody/ -v
jacamo <<< 'agent run-as rosie {  makeArtifact("newDuster", "myBody.NewDuster", ["/dev/ttyUSB0"], Id); focus(Id); }'

jacamo <<< 'agent run-as rosie { .add_plan("+!sweep : not clean <- .print(limpandoBracoNovo); turnLeftArm; .wait(1000); turnRightArm; .wait(1000); stopArm; .wait(1000); !sweep.") }'




