#!/bin/bash

DATA_DIR=$1
NUM_WORDS=$2
CLASSPATH=./lib/weka.jar

if [ -n "$CP_NBAYES" ]
  then
    CLASSPATH=${CP_NBAYES}
fi

COMMAND="java -XX:+UseConcMarkSweepGC -XX:PermSize=5g -Xmx10g -cp $CLASSPATH weka.classifiers.bayes.NaiveBayesMultinomial -t ./$DATA_DIR/$DATA_DIR.word_vector.tfidf.w$NUM_WORDS.arff -c first -d ./$DATA_DIR/$DATA_DIR.nbayes.w$NUM_WORDS.model.dat"
OUT_PATH="./$DATA_DIR/$DATA_DIR.nbayes.results.w$NUM_WORDS.txt"
echo $COMMAND
$COMMAND > $OUT_PATH

