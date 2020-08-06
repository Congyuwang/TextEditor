#!/bin/bash

BASEDIR=$(dirname "$0")

cd "$BASEDIR" || exit

export JAVA_HOME=`/usr/libexec/java_home -v1.8`

javac -d out/jar/ -sourcepath src/ src/*.java src/**/*.java

mkdir -p out/jar/resource/

cp -R resource out/jar

cd "$BASEDIR/out/jar/" || exit

jar cfe ../editor.jar Editor *

cp ../editor.jar ~/Desktop/
