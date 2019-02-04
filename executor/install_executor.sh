#!/bin/bash

echo " ------ DEPRECATED: use a `benchmark-executor-image` to avoid reinstalling all packages via script -------- "

# install add-apt-repository
sudo apt-get install -y software-properties-common
sudo add-apt-repository -y ppa:openjdk-r/ppa
sudo apt-get update
sudo apt-get upgrade -y

# install java and other deps
sudo apt-get install -y openjdk-8-jdk python python-pip python3 zip unzip tmux vim


# bazel
echo "deb [arch=amd64] http://storage.googleapis.com/bazel-apt stable jdk1.8" | sudo tee /etc/apt/sources.list.d/bazel.list
curl https://bazel.build/bazel-release.pub.gpg | sudo apt-key add -
sudo apt-get update && sudo apt-get install -y bazel

# git
sudo apt-get install -y git
