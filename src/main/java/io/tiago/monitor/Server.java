package io.tiago.monitor;

import io.tiago.monitor.domain.Constants;
import io.tiago.monitor.domain.Message;
import io.tiago.monitor.domain.Node;
import io.tiago.monitor.helper.JsonHelper;
import io.tiago.monitor.service.MemoryDB;
import io.tiago.monitor.service.Monitor;
import io.tiago.monitor.service.WebSocket;
import io.tiago.monitor.validator.Validator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO Testing delete all nodes and add again
public class Server extends AbstractVerticle {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Server());
    }

    @Override
    public void start() {

        Router router = Router.router(vertx);
        router.route("/assets/*").handler(StaticHandler.create("assets"));
        router.route().handler(BodyHandler.create());

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");

        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.OPTIONS);

        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

        router.get("/").handler(this::getAll);
        router.get("/timezones").handler(this::getTimeZones);
        router.get("/:key").handler(this::getOne);
        router.get("/export").handler(this::export);
        router.post("/").handler(this::add);
        router.delete("/:key").handler(this::delete);

        HttpServer server = vertx.createHttpServer();

        server.websocketHandler(handler -> {

            Thread t = new Thread(new WebSocket(handler));
            t.setDaemon(true);
            t.start();
            //handler.handler(data -> System.out.println("Received data " + data.toString("ISO-8859-1")));

        });

        String varPort = System.getenv("PORT");
        int port = varPort == null ? 8001 : Integer.parseInt(varPort);
        LOGGER.info("Monitor running listen on port:" + port);
        server.requestHandler(router).listen(port, "0.0.0.0");
    }

    private void getTimeZones(RoutingContext routingContext) {

        Set<String> zoneIds = ZoneId.getAvailableZoneIds();
        routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(200).end(JsonHelper.encodePrettily(zoneIds));
    }

    private void export(RoutingContext routingContext) {
        LOGGER.info("Exporting nodes");
        MemoryDB db = MemoryDB.instance();
        List<Node> nodes = db.all();
        routingContext.response()
                .putHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
                .putHeader("Content-Disposition", "attachment; filename=\"nodes.txt\"")
                .putHeader(HttpHeaders.TRANSFER_ENCODING, "chunked")
                .end(Json.encodePrettily(nodes));
    }

    private void getAll(RoutingContext routingContext) {
        LOGGER.info("Listing all registered services");
        MemoryDB db = MemoryDB.instance();
        List<Node> nodes = db.all();
        routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(200).end(JsonHelper.encodePrettily(nodes));
    }

    private void getOne(RoutingContext routingContext) {
        String key = routingContext.request().getParam("key");
        LOGGER.info("Getting node by key {}", key);
        Node node = MemoryDB.instance().one(key);
        if (node == null) {
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(404).end(JsonHelper.encodePrettily(new Message(Constants.DATA_NOT_FOUND)));
        }
        routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(200).end(JsonHelper.encodePrettily(node));
    }

    private void add(RoutingContext routingContext) {

        String body = routingContext.getBodyAsString();

        if ("".equals(body)) {
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(400).end(JsonHelper.encodePrettily(new Message(Constants.BODY_NOT_EMPTY)));
            return;
        }

        try {

            Node node = (Node) JsonHelper.readValue(body, Node.class);

            Map<String, String> validations = new Validator().validate(node);

            if (validations.size() == 0) {

                LOGGER.info("Adding node {}", node);

                MemoryDB db = MemoryDB.instance();

                if (db.size() < Constants.MAX_ALLOWED_NODES) {

                    db.add(node);

                    Monitor monitor = new Monitor(node);
                    Thread t = new Thread(monitor);
                    t.setDaemon(true);
                    t.start();

                    routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(201).end(JsonHelper.encodePrettily(new Message(Constants.MSG_OK)));
                } else {
                    routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(400).end(JsonHelper.encodePrettily(new Message(Constants.TOO_MANY_NODES)));
                }

            } else {
                routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(400).end(JsonHelper.encodePrettily(validations));
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(400).end(JsonHelper.encodePrettily(new Message(Constants.MSG_BAD_REQUEST)));
        }
    }

    private void delete(RoutingContext routingContext) {
        String key = routingContext.request().getParam("key");
        LOGGER.info("Deleting node by key {}", key);
        Node node = MemoryDB.instance().one(key);
        if (node == null) {
            routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(404).end(JsonHelper.encodePrettily(new Message(Constants.DATA_NOT_FOUND)));
        }
        MemoryDB.instance().remove(key);
        routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(204).end();
    }
}