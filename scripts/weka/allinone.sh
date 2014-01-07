#!/bin/bash

# assume this script is invoded from the current script dir

DATAFILE_IN_BZ2=$1

# make sure only name is specified, no path
DATA_DIR=$2

REGION_CODE=$3

if [ -z "$DATAFILE_IN_BZ2" ];
  then
    echo "Please specify a bz2 data file!"
    exit 0
fi

if [ -z "$DATA_DIR" ];
  then
    echo "Please specify the directory to place data files!"
    exit 0
fi

SCRIPTDIR=$(pwd)
SCRIPT_UTILS_DIR="$SCRIPTDIR/utils"
DEST_ARFF_FILE="$DATA_DIR/$DATA_DIR.arff"

echo "Step 1: generate lables.json"
COMMAND="python utils/labels.py $DATAFILE_IN_BZ2"
echo $COMMAND
$COMMAND

echo "Step 2: convert tsv to arff"
COMMAND="python tsvtoarff.py $DATAFILE_IN_BZ2 $DEST_ARFF_FILE $REGION_CODE"
echo $COMMAND
$COMMAND

echo "Step 3: batch generate models"
COMMAND="./batch_generate_model.sh $DATA_DIR $REGION_CODE"
echo $COMMAND
nohup $COMMAND &
