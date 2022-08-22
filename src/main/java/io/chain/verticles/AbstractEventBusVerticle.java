package io.chain.verticles;

import io.chain.p2p.EventBusAddresses;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractEventBusVerticle extends AbstractVerticle {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractEventBusVerticle.class);

    private List<MessageConsumer<?>> eventBusConsumers = new ArrayList<>();

    @Override
    public void stop(Promise<Void> stopPromise) {
        final List<Future> futures = new ArrayList<>();
        eventBusConsumers.forEach(c -> futures.add(c.unregister()));

        CompositeFuture
                .all(futures)
                .onSuccess(r -> stopPromise.complete())
                .onFailure(stopPromise::fail);
    }

    protected final <T> void register(EventBusAddresses address, Handler<Message<T>> handler) {
        eventBusConsumers.add(vertx.eventBus().consumer(address.getAddress(), handler));
    }

    protected final <T> void registerLocally(EventBusAddresses address, Handler<Message<T>> handler) {
        eventBusConsumers.add(vertx.eventBus().localConsumer(address.getAddress(), handler));
    }
}
