#! /bin/bash
java -cp javacc-7.0.13.jar javacc -OUTPUT_DIRECTORY=../../src/main/java/jacamo/project/parser ../../maop/jacamo/src/main/javacc/JaCaMoProjectParser.jj
java -cp javacc-7.0.13.jar javacc -OUTPUT_DIRECTORY=../../src/main/java/jason/asSyntax/parser ../../maop/jason/jason-interpreter/src/main/javacc/jason/asSyntax/parser/AS2JavaParser.jj
java -cp javacc-7.0.13.jar javacc -OUTPUT_DIRECTORY=../../src/main/java/jason/mas2j/parser ../../maop/jason/jason-interpreter/src/main/javacc/jason/mas2j/parser/MAS2JavaParser.jj
java -cp javacc-7.0.13.jar javacc -OUTPUT_DIRECTORY=../../src/main/java/npl/parser ../../maop/npl/src/main/javacc/npl.jj

