package io.chain.models;

import com.starkbank.ellipticcurve.PublicKey;
import lombok.Getter;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public final class UTxOSet {

    private final Map<Input, UTxO> utxos;

    public UTxOSet() { utxos = new HashMap<>(); }
    public UTxOSet(UTxOSet set) {
        utxos = new HashMap<>() {{
            putAll(set.utxos);
        }};
    }

    public void add(UTxO utxo) {
        utxos.put(utxo.getTxIn(), utxo);
    }

    public void remove(Input in) {
        utxos.remove(in);
    }

    public UTxO get(Input in) { return getUtxos().get(in); }

    public Map<Input, UTxO> getUtxos() {
        return unmodifiableMap(utxos);
    }

    public boolean contains(Input in) {
        return utxos.containsKey(in);
    }
    public boolean contains(UTxO utxo) {
        return contains(utxo.getTxIn());
    }

    public Set<UTxO> filterBy(PublicKey pk) {
        return getUtxos()
                .values()
                .parallelStream()
                .reduce(new HashSet<>(), (set, utxo) -> {
                    if (utxo.getTxOut().getAddress().equals(pk)) {
                        set.add(utxo);
                    }
                    return set;
                }, (set1, set2) -> {
                    set1.addAll(set2);
                    return set1;
                });
    }

    public List<Transaction> process(List<Transaction> txs) {
        final Set<Transaction> processed = new HashSet<>();

        for (final Transaction tx: txs) {
            if (Transaction.isValid(tx, this)) {
                for (Input input: tx.getInputs())
                    remove(input);
                for (int i = 0; i < tx.getOutputs().size(); i++) {
                    final Output output = tx.getOutputs().get(i);
                    final UTxO utxo = new UTxO(new Input(tx.hash().getBytes(), i), output);
                    add(utxo);
                }
                processed.add(tx);
            }
        }
        return unmodifiableList(new ArrayList<>(processed));
    }

}
