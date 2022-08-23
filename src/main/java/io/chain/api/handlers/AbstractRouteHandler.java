package io.chain.api.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.time.format.DateTimeFormatter;
import java.util.Date;

import static io.vertx.core.http.HttpHeaders.createOptimized;
import static java.time.ZoneId.of;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.ENGLISH;

abstract class AbstractRouteHandler {

    static final DateTimeFormatter formatter = ofPattern("EEE, dd MMM yyyy HH:mm:ss z", ENGLISH)
                                                .withZone(of("GMT"));
    static final CharSequence APPLICATION_JSON = createOptimized("application/json; charset=utf-8");


    protected final HttpServerResponse addResponseHeaders(HttpResponseStatus status, RoutingContext context) {
        return context
                .response()
                .setStatusCode(status.code())
                .setStatusMessage(status.reasonPhrase())
                .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .putHeader(HttpHeaders.DATE, formatter.format(new Date().toInstant()))
                ;
    }

    protected final String error(String msg) {
        return new JsonObject().put("error", msg).encodePrettily();
    }
}
