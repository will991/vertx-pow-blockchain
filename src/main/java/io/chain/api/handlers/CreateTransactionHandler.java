package io.chain.api.handlers;

import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.utils.ByteString;
import io.chain.models.Transaction;
import io.chain.models.UTxOSet;
import io.chain.models.Wallet;
import io.chain.models.exceptions.InsufficientUTxOBalanceException;
import io.chain.models.exceptions.TransactionValidationException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Hex;

import static io.chain.p2p.EventBusAddresses.NEW_TRANSACTION;

@RequiredArgsConstructor
public final class CreateTransactionHandler extends AbstractRouteHandler implements Handler<RoutingContext> {

    private final Vertx vertx;
    private final Wallet wallet;
    private final UTxOSet utxos;

    @Override
    public void handle(RoutingContext routingContext) {
        try {
            final JsonObject params = routingContext.body().asJsonObject();
            if (params == null || params.isEmpty() || params.getInteger("amount") == null) {
                addResponseHeaders(HttpResponseStatus.BAD_REQUEST, routingContext)
                    .end(error("Please provide an 'amount' to send."));
                return;
            }

            final int amount = params.getInteger("amount");
            final String hexAddress = params.getString("recipient");
            if (hexAddress == null || hexAddress.isEmpty()) {
                addResponseHeaders(HttpResponseStatus.BAD_REQUEST, routingContext)
                    .end(error("No 'recipient' defined."));
                return;
            }
            final PublicKey recipient = PublicKey.fromString(new ByteString(Hex.decode(hexAddress)));

            byte[] data = null;
            if (params.getValue("data") != null) {
                data = params.getValue("data").toString().getBytes();
            }

            try {
                final Transaction unsignedTx = wallet.create(recipient, amount, data);
                final Transaction signedTx = wallet.sign(unsignedTx);

                try {
                    Transaction.validate(signedTx, utxos);
                    vertx
                        .eventBus()
                        .publish(NEW_TRANSACTION.getAddress(), signedTx.toJson().encode());

                    vertx.setTimer(300L, t -> routingContext.next());
                } catch (TransactionValidationException e) {
                    addResponseHeaders(HttpResponseStatus.BAD_REQUEST, routingContext)
                        .end(e.toJson().encodePrettily());
                }
            } catch (InsufficientUTxOBalanceException e) {
                addResponseHeaders(HttpResponseStatus.BAD_REQUEST, routingContext)
                        .end(error(e.getMessage()));
            }
        } catch (Exception e) {
            addResponseHeaders(HttpResponseStatus.BAD_REQUEST, routingContext)
                .end(error(e.getMessage()));
        }
    }

    private String error(String msg) {
        return new JsonObject().put("error", msg).encodePrettily();
    }
}
