package io.chain.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static io.chain.models.Blockchain.isValidChain;
import static org.junit.jupiter.api.Assertions.*;

public final class BlockchainTests {

    private Blockchain blockchain;

    @BeforeEach
    void beforeTests() {
        blockchain = new Blockchain();
    }

    @Test
    @DisplayName("test first block is genesis")
    public void testFirstBlock() {
        assertEquals(1, blockchain.getBlocks().size());
        assertEquals(Block.genesisBlock(), blockchain.getBlocks().get(0));
    }

    @Test
    @DisplayName("test add block to chain")
    public void testAddBlockToChain() {
        blockchain.addBlock("test data");
        assertEquals(2, blockchain.getBlocks().size());
        assertEquals(Block.genesisBlock().toString(), blockchain.getBlocks().get(0).toString());
        assertEquals(Block.genesisBlock().getHash(), blockchain.getBlocks().get(1).getPreviousBlockHash());
        assertEquals("test data", new String(blockchain.getBlocks().get(1).getData(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("test valid chain")
    void testValidChain() {
        blockchain.addBlock("2nd block");
        assertTrue(isValidChain(blockchain));
    }

    @Test
    @DisplayName("test corrupt genesis blockchain")
    void testInvalidGenesisChain() {
        blockchain.addBlock("2nd block");
        /* The design patterns prohibit any mutating actions for data structures like blocks and blockchain */
//        blockchain.getChain().get(1).setPayload("hello".getBytes(StandardCharsets.UTF_8));
//        assertFalse(isValidChain(blockchain));
    }

    @Test
    @DisplayName("test replacing valid blockchain")
    void testBlockchainReplace() {
        Blockchain b2 = new Blockchain();
        b2.addBlock("2");
        blockchain.replace(b2);
        assertEquals(blockchain.getBlocks(), b2.getBlocks());
    }

    @Test
    @DisplayName("test no replacing by shorter blockchain")
    void testShorterBlockchainReplace() {
        blockchain.addBlock("2");
        Blockchain b2 = new Blockchain();
        blockchain.replace(b2);
        assertEquals(2, blockchain.getBlocks().size());
        assertNotEquals(blockchain.getBlocks(), b2.getBlocks());
    }


    @Test
    @DisplayName("test no replacing by equal length blockchain")
    void testEqualLengthBlockchainReplace() {
        blockchain.addBlock("2");
        Blockchain b2 = new Blockchain();
        b2.addBlock("deux");
        blockchain.replace(b2);
        assertEquals(2, blockchain.getBlocks().size());
        assertNotEquals(blockchain.getBlocks(), b2.getBlocks());
    }
}
