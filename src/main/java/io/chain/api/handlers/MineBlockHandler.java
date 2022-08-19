package io.chain.api.handlers;

import io.chain.models.Block;
import io.chain.models.Blockchain;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MineBlockHandler implements Handler<RoutingContext> {
    private final static Logger LOGGER = LoggerFactory.getLogger(MineBlockHandler.class);

    private Blockchain blockchain;
    private Vertx vertx;

    @Override
    public void handle(RoutingContext context) {
        final Block block = blockchain.addBlock(context.body().buffer().getBytes());
        LOGGER.info(String.format("Added block: %s", block.getHash()));
        vertx.eventBus().publish("io.chain.block.new", blockchain.toJson());
        context.next();
    }
}
