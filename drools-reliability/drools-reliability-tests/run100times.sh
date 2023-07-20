#!/bin/bash

# Replace 'your_command_here' with the actual command you want to run 100 times
command_to_run="mvn surefire:test@default-test -Dtest=ReliabilityFireAndAlarmTest"

# Loop 100 times and run the command each time
for ((i = 1; i <= 100; i++)); do
  echo "Running iteration $i"
  $command_to_run
done

