package io.chain.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.chain.models.Block.genesisBlock;
import static java.util.Collections.singletonList;

@Getter
@ToString
@NoArgsConstructor
public final class Blockchain {

    /*
     * Properties
     */

    private List<Block> chain = new ArrayList<>(singletonList(genesisBlock()));

    /*
     * Methods
     */

    public Block addBlock(byte[] data) {
        final Block newBlock = Block.mineBlock(chain.get(chain.size() - 1), data);
        chain.add(newBlock);
        return newBlock;
    }

    public String toJson() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(chain);
    }

    public Block addBlock(String data) {
        return addBlock(data.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isValidChain(Blockchain chain) {
        if ( ! chain.getChain().get(0).toString().equals(genesisBlock().toString())) return false;
        for (int i = 1; i < chain.getChain().size(); i++) {
            final Block block = chain.getChain().get(i);
            final Block lastBlock = chain.getChain().get(i - 1);

            if ( ! block.getPreviousBlockHash().equals(lastBlock.getHash())) return false;
            if ( ! block.getHash().equals(Block.hash(block))) return false;
        }

        return true;
    }

    public void replace(Blockchain newChain) {
        if (newChain.getChain().size() <= chain.size()) return;
        if ( ! isValidChain(newChain)) return;
        chain = newChain.getChain();
    }
}
