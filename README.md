# PoW Will-Chain

## REST API

`GET /blocks` Returns list of all blocks in blockchain.

`GET /blockCount` Returns height of blockchain.

`GET/block/:blockHash` Returns the respective block or error.

`GET /transaction/:txHash` Returns the respective transaction or error.

`POST /transaction` Creates a new transaction that spends a given amount from the miner wallet to
a specified recipient if the balance is sufficient. The POST request payload should look something like this:
```
{
    "amount": <number>,
    "recipient": "<hex public key>",
    "data": <arbitrary data>         // optional 
}
```
This endpoint redirects to the Tx mem pool if successful.

`GET /mempool` Returns the list of signed, unconfirmed transactions.

`GET /miner` Returns miner information like public key and current balance.

`POST /mine` Mines a new block and redirects to `/blocks` endpoint.

`GET /utxos` Returns the global UTxO set of the blockchain that is spendable.

## Building & Dependencies
For development the [jenv](https://github.com/jenv/jenv) repo was used to selectively manage Java versions on a per project level.
- Apache Maven 3.8.1
- Java 14

In order to rebuild the fat jar that was added to the repo, you will need to execute the following:

This will install all dependencies from the `pom.xml` to your local maven repo.

```
mvn -U dependency:resolve
```

This will compile, test and package the entire software to a fat jar which includes all dependencies into 
`target/will-chain-0.0.1-SNAPSHOT.jar`.

```
mvn clean package
```

## Configuration
There are a few available configurations which can be found in
`src/main/resources/config.json`:

```
{
  "http": {
    "host": "localhost",
    "port": 8080
  },
  "isMiner": true,
  "initialUTxOs": []
}
```

One can change the default port and set an initial set of available UTxOs,
which is primarily used for testing purposes. Running the blockchain creates
new spendable UTxOs through the process of mining.

## Running the blockchain
You can choose to run a blockchain node with the default configuration by executing the script below from the 
project root directory or provide a custom port. Running the same script with different
ports will boot multiple nodes that will find each other, unless there are firewalls blocking
discovery requests.

```
./scripts/run.sh <port | default 8080>
``` 

## Testing
This requires maven to be installed on your machine. During development, **Apache Maven 3.8.1** was used. 
Then, you can run the following command in the root directory of the project:

```
mvn clean test
```

## Chain Properties vs predefined Requirements


## Improvements
- sync single block instead of entire blockchain
- addition of transaction fees for miners
- dynamic difficulty adjustment
- decreasing miner reward
- persist blockchain in Postgres instead of in-memory
- add block data as plain byte[] instead of json to decrease size
  - requires custom byte-wise deserialization logic
- add sync mechanism for range of blocks based on height difference
- use websockets instead of VertX event bus for sync of blocks and txs