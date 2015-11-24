package org.kevoree.modeling.addons.rest;

import io.undertow.Undertow;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KModel;
import org.kevoree.modeling.KObject;

public class RestGateway implements HttpHandler {

    private int _port;
    private Undertow _server;
    private KModel _model;

    public RestGateway(KModel p_model, int p_port) {
        this._port = p_port;
        this._model = p_model;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        String rawPath = httpServerExchange.getRelativePath();
        if (rawPath.startsWith("/")) {
            rawPath = rawPath.substring(1);
        }
        String[] parts = rawPath.split("/");
        long universe = -1;
        long time = -1;
        if (parts.length >= 3) {
            universe = Long.parseLong(parts[0]);
            time = Long.parseLong(parts[1]);
            StringBuilder concatQuery = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                if (concatQuery.length() > 0) {
                    concatQuery.append(" | ");
                }
                concatQuery.append(parts[i]);
            }
            httpServerExchange.dispatch();
            _model.universe(universe).time(time).select(concatQuery.toString(), new KCallback<Object[]>() {
                @Override
                public void on(Object[] objects) {
                    httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    Sender sender = httpServerExchange.getResponseSender();
                    StringBuilder builder = new StringBuilder();
                    builder.append("[\n");
                    for (int i = 0; i < objects.length; i++) {
                        if (i != 0) {
                            builder.append(",\n");
                        }
                        builder.append(((KObject) objects[i]).toString());
                    }
                    builder.append("\n]\n");
                    sender.send(builder.toString());
                    httpServerExchange.endExchange();
                }
            });
        } else {
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            httpServerExchange.getResponseSender().send("Bad URL format");
        }
    }

    public static RestGateway expose(KModel model, int port) {
        RestGateway newgateway = new RestGateway(model, port);
        return newgateway;
    }

    public void start() {
        _server = Undertow.builder().addHttpListener(_port, "0.0.0.0").setHandler(this).build();
        _server.start();
    }

}
