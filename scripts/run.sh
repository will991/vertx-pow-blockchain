#!/bin/bash

################################## Helper ##################################

exit_with_error() {
    printf "'%s' failed with exit code %d in function '%s' at line %d.\n" "${1-something}" "$?" "${FUNCNAME[1]}" "${BASH_LINENO[0]}"
    exit 1
}

################################## RUN ##################################

if [ -z "$1" ]; then
    echo "No config file specified - defaulting to ./src/main/resources/config.json"
fi

java -jar target/will-chain-0.0.1-SNAPSHOT.jar -cluster -conf './src/main/resources/config.json' -Djava.net.preferIPv4Stack=true
