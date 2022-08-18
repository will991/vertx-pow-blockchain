package io.chain.models;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.GregorianCalendar;

@Value
@Builder
@ToString
public class Block {

    /*
     * Properties
     */

    long timestamp;
    String hash;
    String previousBlockHash;
    byte[] data;

    /* TODO: Add transactions payload */
    /* TODO: Add metadata payload */

    /*
     * Static Methods
     */

    public static Block genesisBlock() {
        final long start = new GregorianCalendar(1991,11,3).getTimeInMillis();
        final byte[] data = "Will is building his own chain".getBytes(StandardCharsets.UTF_8);
        return builder()
                .timestamp(start)
                .hash(hash(start, "WillWasStillUnborn", data))
                .previousBlockHash("WillWasStillUnborn")
                .data(data)
                .build();
    }

    /**
     * Creates a new block that is linked to the given previous block.
     * @param lastBlock defines the previous block.
     * @return a new block with <code>previousBlockHash</code> set to the provided <code>lastBlock</code>
     */
    public static Block mineBlock(Block lastBlock, byte[] data) {
        final long minedAt = System.currentTimeMillis();
        Block newBlock = builder()
                .timestamp(minedAt)
                .previousBlockHash(lastBlock.getHash())
                .hash(hash(minedAt, lastBlock.getHash(), data))
                .data(data)
                .build();

        return newBlock;
    }

    public static String hash(long timestamp, String previousBlockHash, byte[] data) {
        final StringBuilder sb = new StringBuilder()
                .append(timestamp)
                .append(previousBlockHash)
                .append(new String(data, StandardCharsets.UTF_8));
        return DigestUtils.sha3_256Hex(sb.toString());
    }

    public static String hash(Block block) {
        return hash(block.getTimestamp(), block.getPreviousBlockHash(), block.getData());
    }
}
