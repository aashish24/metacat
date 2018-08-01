#!/bin/bash

# find-maven.sh
# Author: Bryce Mecum <mecum@nceas.ucsb.edu>
#
# Summary: Try to find a suitabe MAVEN_HOME and echo the location. This script
#          fed into ant via an Exec call to dynamically set an ant build 
#          variable 'maven.home'
#
#          The following algorithm is used:
#
#          1. If MAVEN_HOME is set, use that
#          2. If mvn can be found by the script, grab the value from 
#             mvn --version
#          3. If mvn3 can be found by the script, grab the value from 
#             mvn3 --version
#          4. Fail with a hopefully helpful error message

set -e
set -u

# First choice: Trust MAVEN_HOME
if [ ! -z ${MAVEN_HOME+x} ]
then 
  echo "$MAVEN_HOME"
  exit 0
fi

# Second choice: Ask mvn
if command -v mvn > /dev/null 2>&1
then
  mvn --version | grep "Maven home" | cut -d" " -f3
  exit 0
fi

# Third choice: Ask mvn3
if command -v mvn3 > /dev/null 2>&1
then
  mvn3 --version | grep "Maven home" | cut -d" " -f3
  exit 0
fi

# Fail with help message
printf "A proper MAVEN_HOME could not be found. Either:\\n\\n"
printf "  - Set MAVEN_HOME i.e., export MAVEN_HOME=\"{SOMETHING}\"\\n"
printf "  - Make your mvn executable available in your PATH\\n\\n"
printf "And then run this script (or ant if that's the command you just ran\n"
printf "when you got this error).\\n"
exit 1
