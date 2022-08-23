package io.chain.models;

import lombok.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Objects;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Block {

    public static final int MINING_REWARD = 100;
    private static final int DIFFICULTY = 4;
    private static String DIFFICULTY_PREFIX;
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DIFFICULTY; i++) {
            sb.append("0");
        }
        DIFFICULTY_PREFIX = sb.toString();
    }

    /*
     * Properties
     */

    long timestamp;
    String hash;
    String previousBlockHash;
    byte[] data;
    int nonce;

    /* TODO: Add transactions payload */
    /* TODO: Add metadata payload */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return timestamp == block.timestamp && nonce == block.nonce && Objects.equals(hash, block.hash) && Objects.equals(previousBlockHash, block.previousBlockHash) && Arrays.equals(data, block.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(timestamp, hash, previousBlockHash, nonce);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    /*
     * Static Methods
     */

    public static Block genesisBlock() {
        final long start = new GregorianCalendar(1991,11,3).getTimeInMillis();
        final byte[] data = "Will is building his own chain".getBytes();
        return builder()
                .timestamp(start)
                .hash(hash(start, "WillWasStillUnborn", data, 0))
                .previousBlockHash("WillWasStillUnborn")
                .data(data)
                .nonce(0)
                .build();
    }

    /**
     * Creates a new block that is linked to the given previous block.
     * @param lastBlock defines the previous block.
     * @return a new block with <code>previousBlockHash</code> set to the provided <code>lastBlock</code>
     */
    public static Block mineBlock(Block lastBlock, byte[] data) {
        long minedAt;
        int nonce = 0;
        String hash;

        do {
            nonce++;
            minedAt = System.currentTimeMillis();
            hash = hash(minedAt, lastBlock.getHash(), data, nonce);
        } while ( ! hash.startsWith(DIFFICULTY_PREFIX));

        return builder()
                .timestamp(minedAt)
                .previousBlockHash(lastBlock.getHash())
                .hash(hash)
                .data(data)
                .nonce(nonce)
                .build();
    }

    public static String hash(long timestamp, String previousBlockHash, byte[] data, int nonce) {
        final StringBuilder sb = new StringBuilder()
                .append(timestamp)
                .append(previousBlockHash)
                .append(new String(data, StandardCharsets.UTF_8))
                .append(nonce);
        return DigestUtils.sha3_256Hex(sb.toString());
    }

    public static String hash(Block block) {
        return hash(block.getTimestamp(), block.getPreviousBlockHash(), block.getData(), block.getNonce());
    }
}
