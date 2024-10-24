#!/bin/sh

cd "$(dirname "$(readlink -fn "$0")")" || exit 1

# check if java is installed
if [ -x "$(command -v java)" ]; then
  # if screen is present use that
  # this check is elevated as tmux is sometimes present by default
  if [ -x "$(command -v screen)" ]; then
    # DO NOT CHANGE THE SUPPLIED MEMORY HERE. THIS HAS NO EFFECT ON THE NODE INSTANCE. USE THE launcher.cnl INSTEAD
    screen -DRSq CloudNet java -Xms128M -Xmx128M -XX:+UseZGC -XX:+PerfDisableSharedMem -jar launcher.jar
  elif [ -x "$(command -v tmux)" ]; then
    # DO NOT CHANGE THE SUPPLIED MEMORY HERE. THIS HAS NO EFFECT ON THE NODE INSTANCE. USE THE launcher.cnl INSTEAD
    tmux new-session -As CloudNet java -Xms128M -Xmx128M -XX:+UseZGC -XX:+PerfDisableSharedMem -jar launcher.jar
  else
    echo "No screen or tmux installation found, you need to install at least one of them to run CloudNet"
    exit 1
  fi
else
  echo "No valid java installation was found, please install java in order to run CloudNet"
  exit 1
fi
