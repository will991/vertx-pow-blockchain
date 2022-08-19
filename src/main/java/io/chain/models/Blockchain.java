package io.chain.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.chain.models.Block.genesisBlock;
import static io.vertx.core.buffer.Buffer.buffer;
import static java.util.Collections.singletonList;

@Getter
@ToString
public final class Blockchain {

    /*
     * Properties
     */

    private List<Block> blocks;

    public Blockchain() {
        blocks = new ArrayList<>(singletonList(genesisBlock()));
    }

    public Blockchain(List<Block> blocks) {
        this.blocks = blocks;
    }

    /*
     * Methods
     */

    public Block addBlock(byte[] data) {
        final Block newBlock = Block.mineBlock(blocks.get(blocks.size() - 1), data);
        blocks.add(newBlock);
        return newBlock;
    }

    public JsonArray toJson() {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return buffer(mapper.writeValueAsString(blocks)).toJsonArray();
        } catch (JsonProcessingException e) {
            return new JsonArray().add(new JsonObject().put("error", e.getMessage()));
        }
    }

    public Block addBlock(String data) {
        return addBlock(data.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isValidChain(Blockchain chain) {
        if ( ! chain.getBlocks().get(0).equals(genesisBlock())) return false;
        for (int i = 1; i < chain.getBlocks().size(); i++) {
            final Block block = chain.getBlocks().get(i);
            final Block lastBlock = chain.getBlocks().get(i - 1);

            if ( ! block.getPreviousBlockHash().equals(lastBlock.getHash())) return false;
            if ( ! block.getHash().equals(Block.hash(block))) return false;
        }

        return true;
    }

    public boolean replace(Blockchain newChain) {
        if (newChain.getBlocks().size() <= blocks.size()) return false;
        if ( ! isValidChain(newChain)) return false;
        blocks = newChain.getBlocks();
        return true;
    }
}
