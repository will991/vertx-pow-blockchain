package io.chain.verticles;

import io.chain.models.Blockchain;
import io.chain.p2p.handlers.BlockchainSyncHandler;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.chain.p2p.EventBusAddresses.NEW_BLOCKCHAIN;
import static java.lang.String.format;

@Getter
@RequiredArgsConstructor
public final class BlockchainSyncVerticle extends AbstractVerticle {

    private final Blockchain blockchain;
    private final String uuid = UUID.randomUUID().toString();
    private List<MessageConsumer<?>> eventBusConsumers = new ArrayList<>();

    @Override
    public void start() {
        register(NEW_BLOCKCHAIN.getAddress(), msg -> {
            new BlockchainSyncHandler(uuid, blockchain).handle(((JsonArray) msg.body()).toBuffer());
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        final List<Future> futures = new ArrayList<>();
        eventBusConsumers.forEach(c -> futures.add(c.unregister()));

        CompositeFuture
                .all(futures)
                .onSuccess(r -> stopPromise.complete())
                .onFailure(stopPromise::fail);
    }

    private <T> void register(String suffix, Handler<Message<T>> handler) {
        eventBusConsumers.add(vertx.eventBus().consumer(address(suffix), handler));
    }

    private String address(String suffix) {
        return format("io.chain.%s", suffix);
    }
}
