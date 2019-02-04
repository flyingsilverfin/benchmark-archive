#!/bin/bash

sudo apt-get install -y software-properties-common 
sudo add-apt-repository -y ppa:openjdk-r/ppa 
sudo apt-get update 
sudo apt-get upgrade -y 
sudo apt-get install -y openjdk-8-jdk unzip tmux vim 

curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash - 
sudo apt-get install -y nodejs 


# create gcloud ssh keys to spawn new instances without passphrase
ssh-keygen -t rsa -b 4096 -N "" -f ~/.ssh/google_compute_engine 
