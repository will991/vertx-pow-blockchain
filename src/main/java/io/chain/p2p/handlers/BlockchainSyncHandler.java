package io.chain.p2p.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.chain.models.Block;
import io.chain.models.Blockchain;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;

@Getter
@RequiredArgsConstructor
public final class BlockchainSyncHandler implements Handler<Message<JsonArray>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockchainSyncHandler.class);

    private final String uuid;
    private final Blockchain blockchain;

    @Override
    public void handle(Message<JsonArray> msg) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            List<Block> blocks = mapper.readValue(msg.body().encode(), new TypeReference<>(){});
            Blockchain newBlockchain = new Blockchain(blocks);

            if (blockchain.replace(newBlockchain)) {
                LOGGER.info(format("[%s] Replacing chain with new one:%s%s", uuid, lineSeparator(), newBlockchain.toJson().encodePrettily()));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
