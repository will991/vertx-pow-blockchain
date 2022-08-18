package io.chain.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.GregorianCalendar;

import static io.chain.models.Block.hash;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class BlockTests {

    private Block genesisBlock;
    private Block block;

    @BeforeEach
    void beforeTest() {
        genesisBlock = Block.genesisBlock();
        block = Block.mineBlock(genesisBlock, "test data".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("test correct genesis block")
    void testGenesisBlock() {
        final long bornAt = new GregorianCalendar(1991,11,3).getTimeInMillis();
        final String data = "Will is building his own chain";
        assertEquals(bornAt, genesisBlock.getTimestamp());
        assertEquals("WillWasStillUnborn", genesisBlock.getPreviousBlockHash());
        assertEquals(data, new String(genesisBlock.getData(), StandardCharsets.UTF_8));
        assertEquals(hash(bornAt, "WillWasStillUnborn", data.getBytes(StandardCharsets.UTF_8)), genesisBlock.getHash());
    }

    @Test
    @DisplayName("test valid block building")
    void testCreateBlock() {
        final long now = 1000L;
        final Block block = Block.builder()
                                .hash("1")
                                .previousBlockHash("0")
                                .timestamp(now)
                                .build();
        assertEquals(
            format("Block(timestamp=%d, hash=%d, previousBlockHash=%d, data=null)", now, 1, 0),
            block.toString()
        );
    }

    @Test
    @DisplayName("test block hashing and linking")
    void testBlockLinks() {
        /**
         * REMARK:
         * Cannot directly test <code>Block.mine</code> method due to hardcoded <i>block mining timestamp</i>.
         */
        final long now = System.currentTimeMillis();
        final String hash = hash(now, genesisBlock.getHash(), "test data".getBytes(StandardCharsets.UTF_8));
        final Block newBlock = Block.builder()
                                .hash(hash)
                                .previousBlockHash(genesisBlock.getHash())
                                .timestamp(now)
                                .build();
        assertEquals(genesisBlock.getHash(), newBlock.getPreviousBlockHash());
        assertEquals(hash, newBlock.getHash());
    }

    @Test
    @DisplayName("test block hash")
    void testBlockHash() {
        assertEquals(genesisBlock.getHash(), Block.hash(genesisBlock));
    }
}
