package io.chain.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static io.chain.models.Block.*;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class BlockTests {

    private Block genesisBlock;
    private Block block;

    @BeforeEach
    void beforeTest() {
        genesisBlock = genesisBlock();
        block = mineBlock(genesisBlock, "test data".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("test correct genesis block")
    void testGenesisBlock() {
        final long bornAt = new GregorianCalendar(1991, Calendar.NOVEMBER,3).getTimeInMillis();
        final String data = "Will is building his own chain";
        assertEquals(bornAt, genesisBlock.getTimestamp());
        assertEquals("WillWasStillUnborn", genesisBlock.getPreviousBlockHash());
        assertEquals(data, new String(genesisBlock.getData(), StandardCharsets.UTF_8));
        assertEquals(hash(bornAt, "WillWasStillUnborn", data.getBytes(StandardCharsets.UTF_8), 0), genesisBlock.getHash());
    }

    @Test
    @DisplayName("test valid block building")
    void testCreateBlock() {
        final long now = 1000L;
        final Block block = Block.builder()
                                .previousBlockHash("0")
                                .timestamp(now)
                                .build();
        assertEquals(
            format("Block(timestamp=%d, previousBlockHash=%d, data=null, nonce=0)", now, 0),
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
        final String hash = hash(now, genesisBlock.getHash(), "123".getBytes(), 0);
        final Block newBlock = Block.builder()
                                .previousBlockHash(genesisBlock.getHash())
                                .timestamp(now)
                                .nonce(0)
                                .data("123".getBytes())
                                .build();
        assertEquals(genesisBlock.getHash(), newBlock.getPreviousBlockHash());
        assertEquals(hash, newBlock.getHash());
    }

    @Test
    @DisplayName("test block hash")
    void testBlockHash() {
        assertEquals(genesisBlock.getHash(), Block.hash(genesisBlock));
    }

    @Test
    @DisplayName("test correct hash with difficulty")
    void testBlockHashDifficulty() {
        Block block = mineBlock(genesisBlock(), "test".getBytes(StandardCharsets.UTF_8));
        assertTrue(block.getHash().startsWith("0000"));
    }
}
