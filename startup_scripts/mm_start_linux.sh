#!/bin/sh

MM_PATH=$(pwd)
IJ_LIB=$MM_PATH/lib/micro-manager
TOOLS_JAR_PATH=$(find /usr -name tools.jar)

export LD_LIBRARY_PATH=.:$IJ_LIB:$LD_LIBRARY_PATH
export CLASSPATH=$MM_PATH/plugins/Micro-Manager

java -mx4000m \
     -Djava.library.path=$IJ_LIB \
     -Dplugins.dir=$MM_PATH \
     -cp ij.jar:$TOOLS_JAR_PATH \
     ij.ImageJ
