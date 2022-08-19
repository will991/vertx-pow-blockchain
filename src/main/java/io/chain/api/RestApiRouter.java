package io.chain.api;

import io.chain.api.handlers.GetBlockCountHandler;
import io.chain.api.handlers.GetBlocksHandler;
import io.chain.api.handlers.MineBlockHandler;
import io.chain.models.Blockchain;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerFormat;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.impl.RouterImpl;

public final class RestApiRouter extends RouterImpl {

    public RestApiRouter(Vertx vertx, Blockchain blockchain) {
        super(vertx);

        route()
            .handler(LoggerHandler.create(LoggerFormat.SHORT))
            .handler(CorsHandler.create("*"));

        get("/blocks")
            .handler(new GetBlocksHandler(blockchain))
            .setName("Get Blocks");

//        get("/block/:blockHash")
//        get("/transaction/:txHash")
//        post("/transaction")

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
