#!/bin/bash

DATA_DIR=$1
NUM_WORDS=$2
REGION_CODE=$3

SCRIPTDIR=$(pwd)

echo "Step 1: convert data to vector"
COMMAND="./data2vector.sh $DATA_DIR $NUM_WORDS $REGION_CODE"
echo $COMMAND
$COMMAND

echo "Step 2: run nbayes"
COMMAND="./nbayes.sh $DATA_DIR $NUM_WORDS"
echo $COMMAND
$COMMAND

echo "Step 3: generate json from nbays result"
COMMAND="./utils/prob2json.py $DATA_DIR/$DATA_DIR.nbayes.results.w$NUM_WORDS.txt $DATA_DIR/model.$NUM_WORDS.js"
echo $COMMAND
$COMMAND

