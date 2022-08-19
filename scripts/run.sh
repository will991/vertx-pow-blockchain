#!/bin/bash

################################## Helper ##################################

exit_with_error() {
    printf "'%s' failed with exit code %d in function '%s' at line %d.\n" "${1-something}" "$?" "${FUNCNAME[1]}" "${BASH_LINENO[0]}"
    exit 1
}

################################## RUN ##################################

if [ -z "$1" ]; then
    echo "No port number specified - defaulting to 8080"
fi

java -jar target/will-chain-0.0.1-SNAPSHOT.jar -cluster -Djava.net.preferIPv4Stack=true -Drest.http.port=$1
