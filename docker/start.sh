#!/usr/bin/env bash

cd `dirname $0`

REBUILD=$1

DOCKER_IMAGE="bhenk/axxess"
CONTAINER_NAME="axxess"
# Docker volumes can only be mounted on absolute files.
# The configuration directory.
CONFIG_DIR=$PWD/cfg
# The directory for logs
LOG_DIR=$PWD/logs
# An example can be found in
EXAMPLE_DIR=$PWD/example
# Input and output directory
WORK_DIR=$PWD/work

if [ ! -d "$WORK_DIR" ]; then
  echo "Creating output directory $WORK_DIR"
  mkdir -p "$WORK_DIR"
fi

if [[ "$REBUILD" == "-r" ]]; then
  echo "Rebuilding"
  docker rmi $DOCKER_IMAGE
fi

# build image if it does not exist or rebuild requested
if [ "$(docker images -q $DOCKER_IMAGE:latest 2> /dev/null)" == "" ]; then
  echo "Image $DOCKER_IMAGE does not exists. Building it"
  cd ../
  ./docker-build.sh
  cd docker
fi

echo "Starting docker container $CONTAINER_NAME in detached mode"
docker run -d --rm --name $CONTAINER_NAME \
    -v $CONFIG_DIR:/code/cfg \
    -v $LOG_DIR:/code/logs \
    -v $EXAMPLE_DIR:/code/example \
    -v $WORK_DIR:/code/work \
    $DOCKER_IMAGE

echo "Following logs of $CONTAINER_NAME"
docker logs -f $CONTAINER_NAME