#!/bin/bash

DATA_DIR=$1
WORD_NUMS=(1500 3000 5000 7500 10000)
CLASSPATH=./lib/weka.jar:./lib/ChineseTokenizer.jar:./lib/tree_split-1.0.1.jar:./lib/ansj_seg-0.9.jar

for NUM_WORDS in ${WORD_NUMS[*]}
do
    COMMAND="java -XX:+UseConcMarkSweepGC -XX:PermSize=5g -Xmx10g -cp $CLASSPATH weka.filters.unsupervised.attribute.StringToWordVector -tokenizer org.mozilla.up.ChineseTokenizer -C -prune-rate 10 -T -I -N -W $NUM_WORDS -i ./$DATA_DIR/$DATA_DIR.arff -o ./$DATA_DIR/$DATA_DIR.word_vector.tfidf.w$NUM_WORDS.arff"
    echo $COMMAND
    nohup $COMMAND &
done
