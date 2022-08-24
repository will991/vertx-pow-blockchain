package io.chain.p2p.handlers;

import io.chain.models.UTxOSet;
import io.chain.models.Wallet;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SyncWalletHandler implements Handler<Message<Void>> {

    private final Wallet wallet;
    private final UTxOSet utxoSet;

    @Override
    public void handle(Message<Void> voidMessage) {
        try {
            utxoSet.sync(wallet);
        } catch (Wallet.MismatchingUTxOAddressException e) {
            e.printStackTrace(System.err);
        }
    }
}
