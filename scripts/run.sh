#!/bin/bash

################################## Helper ##################################

exit_with_error() {
    printf "'%s' failed with exit code %d in function '%s' at line %d.\n" "${1-something}" "$?" "${FUNCNAME[1]}" "${BASH_LINENO[0]}"
    exit 1
}

################################## RUN ##################################

CONFIG='./src/main/resources/config.json'
if [ -z "$1" ]; then
  echo "No config file specified - defaulting to ./src/main/resources/config.json";
  java -jar target/will-chain-0.0.1-SNAPSHOT.jar -Djava.net.preferIPv4Stack=true -Djava.util.logging.config.file=./src/test/resources/chain.log.conf -cluster -conf $CONFIG
else
  java -jar target/will-chain-0.0.1-SNAPSHOT.jar -Djava.net.preferIPv4Stack=true -Djava.util.logging.config.file=./src/test/resources/chain.log.conf -cluster -conf $CONFIG -Dhttp.port=$1
fi

