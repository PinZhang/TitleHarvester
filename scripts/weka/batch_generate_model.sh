#!/bin/bash

DATA_DIR=$1
REGION_CODE=$2

WORD_NUMS=(1500 3000 5000 7500 10000)

for NUM_WORDS in ${WORD_NUMS[*]}
do
    COMMAND="./generate_model.sh $DATA_DIR $NUM_WORDS $REGION_CODE"
    echo $COMMAND
    nohup $COMMAND &
done

