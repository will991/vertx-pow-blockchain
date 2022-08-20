package io.chain;

import io.chain.models.Blockchain;
import io.chain.verticles.BlockchainSyncVerticle;
import io.chain.verticles.RestApiVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import static java.lang.String.format;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private final Blockchain blockchain = new Blockchain();

    @Override
    public void start(Promise<Void> startPromise) {
        deploy(new RestApiVerticle(blockchain), new DeploymentOptions().setConfig(config()))
            .compose(r -> deploy(new BlockchainSyncVerticle(blockchain), new DeploymentOptions()))
            .onSuccess(startPromise::complete)
            .onFailure(startPromise::fail);
    }

    private <T extends AbstractVerticle> Future<Void> deploy(T verticle, DeploymentOptions opts) {
        Promise<Void> promise = Promise.promise();
        vertx
            .deployVerticle(verticle, opts)
            .onSuccess(result -> {
                LOGGER.info(format("Successfully deployed: %s", verticle.getClass().getSimpleName()));
                promise.complete();
            })
            .onFailure(err -> {
                LOGGER.error(format("Failed deploying: %s", verticle.getClass().getSimpleName()));
                promise.fail(err);
            });
        return promise.future();
    }

}
