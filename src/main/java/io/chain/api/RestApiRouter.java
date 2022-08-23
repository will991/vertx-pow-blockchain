package io.chain.api;

import io.chain.api.handlers.*;
import io.chain.models.Blockchain;
import io.chain.models.UTxOSet;
import io.chain.models.Wallet;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.impl.RouterImpl;

public final class RestApiRouter extends RouterImpl {

    public RestApiRouter(Vertx vertx, Blockchain blockchain, Wallet wallet, UTxOSet utxos) {
        super(vertx);

        route()
            .handler(LoggerHandler.create(LoggerFormat.SHORT))
            .handler(CorsHandler.create("*"));

        get("/miner")
            .handler(new GetMinerPublicKey(wallet))
            .setName("Get miner public key");

        get("/blocks")
            .handler(new GetBlocksHandler(blockchain))
            .setName("Get Blocks");

        get("/mempool")
                .handler(new GetMemPoolTransactionsHandler(vertx))
                .setName("Get MemPool Transactions");

//        get("/block/:blockHash")
//        get("/transaction/:txHash")
        post("/transaction")
            .handler(BodyHandler.create())
            .handler(new CreateTransactionHandler(vertx, wallet, utxos))
            .handler(new GetMemPoolTransactionsHandler(vertx))
            .setName("Add a transaction");

        get("/blockCount")
            .handler(new GetBlockCountHandler(blockchain))
            .setName("Get block count");

        post("/mine")
            .handler(BodyHandler.create())
            .handler(new MineBlockHandler(blockchain, vertx))
            .handler(new GetBlocksHandler(blockchain))
            .setName("Mine a new block");
    }

}
