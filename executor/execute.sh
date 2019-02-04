#!/bin/bash

set -e #Exit immediately if any error occurs so that we dont invoke the /completed, which leads to deletion of current VM

if [ $# -ne 5 ]
then
    echo "Require 5 arguments"
    echo "Usage: ./execute.sh <repo https url> <commit id> <benchmark service IP> <this instance name> <execution id>"
    exit 1;
fi

GRAKN_REPOSITORY_URL=$1
COMMIT=$2
SERVICE_IP=$3
INSTANCE_NAME=$4
EXECUTION_ID=$5

cd ~

# --- get grakn ---
git clone $GRAKN_REPOSITORY_URL

# build grakn
cd grakn
git checkout $COMMIT 
# git checkout $COMMIT

# TODO remove;
# copy over backwards compatible update.sh for now
cp ~/executor/update_backwards_compat.sh dependencies/maven/update.sh
chmod +x dependencies/maven/update.sh

./dependencies/maven/update.sh
bazel build //:distribution --incompatible_remove_native_http_archive=false --incompatible_package_name_is_a_function=false --incompatible_remove_native_git_repository=false

# unzip grakn
cd bazel-genfiles
if [ ! -f grakn-core-all.zip ]
then
    # older versions put the zip under bazel-genfiles/dist
    unzip dist/grakn-core-all.zip 
else
    unzip grakn-core-all.zip 
fi

# start grakn
cd grakn-core-all
./grakn server start --benchmark


# reset dir
cd ~

# --- clone benchmark ---
git clone https://github.com/graknlabs/benchmark.git

# build benchmark
cd benchmark

./dependencies/maven/update.sh
bazel build //:distribution --incompatible_remove_native_git_repository=false --incompatible_remove_native_http_archive=false --incompatible_package_name_is_a_function=false

# unzip benchmark
cd bazel-genfiles
unzip benchmark.zip
cd benchmark

# --- run zipkin ---
tmux new-session -d -s zipkin "STORAGE_TYPE=elasticsearch ES_HOSTS=http://$SERVICE_IP:9200 ES_INDEX=benchmark java -jar external-dependencies/zipkin.jar"


# notify benchmark service about start
echo "Notifying Benchmark Service /start"
curl --header "Content-Type: application/json" \
    --request POST \
    --data "{\"executionId\":\"$EXECUTION_ID\" }" \
    http://$SERVICE_IP:4567/execution/start

# --- run benchmark ---
./benchmark --config ./conf/road_network/road_config.yml --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200
./benchmark --config ./conf/financial_transactions/financial_config.yml --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200
./benchmark --config ./conf/social_network/social_network_config.yml --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200
./benchmark --config ./conf/biochem_network/biochem_config.yml --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200

# TODO report log files

# Call completed hook on the service
curl --header "Content-Type: application/json" \
    --request POST \
    --data "{\"executionId\":\"$EXECUTION_ID\", \"vmName\": \"$INSTANCE_NAME\"}" \
    http://$SERVICE_IP:4567/execution/completed
