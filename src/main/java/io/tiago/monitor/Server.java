package io.tiago.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Server extends AbstractVerticle {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Server());
        LOGGER.info("Monitor running listen on port:" + Constants.APP_PORT);
    }

    @Override
    public void start() {

        Router router = Router.router(vertx);
        router.route("/assets/*").handler(StaticHandler.create("assets"));
        router.route().handler(BodyHandler.create());

        HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create();

        router.get("/").handler(this::getAll);
        router.post("/").handler(this::addOne).handler(validationHandler);

        HttpServer server = vertx.createHttpServer();

        server.websocketHandler(handler -> {
            // TODO Apply refactor: move to a class
            LOGGER.info("Web socket client connected");
            MemoryDb db = MemoryDb.instance();
            Runnable task = () -> {

                while (true) {
                    Map<String, Node> data = db.all();
                    data.forEach((k, v) -> {
                        LOGGER.info(String.format("Sending event %s", k));
                        handler.writeBinaryMessage(Buffer.buffer(k));
                    });
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
            //event.handler(data -> System.out.println("Received data " + data.toString("ISO-8859-1")));
        });

        server.requestHandler(router).listen(Constants.APP_PORT);
    }

    private void getAll(RoutingContext routingContext) {
        LOGGER.info("Listing all registered services");
        routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(200).end(Json.encodePrettily(new Message(Constants.MSG_OK)));
    }

    private void addOne(RoutingContext routingContext) {

        String body = routingContext.getBodyAsString();

        if ("".equals(body)) {
            routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(400).end(Json.encodePrettily(new Message(Constants.BODY_NOT_EMPTY)));
            return;
        }

        Node node;

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
            node = mapper.readValue(body, Node.class);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(400).end(Json.encodePrettily(new Message(Constants.MSG_BAD_REQUEST)));
            return;
        }

        Map<String, String> validations = new Validator().validate(node);

        if (validations.size() == 0) {

            try {

                LOGGER.info(String.format("Adding node %s", node));

                NodeQueue nodeQueue = NodeQueue.instance();

                if (nodeQueue.size() < Constants.MAX_ALLOWED_NODES) {

                    NodeQueue.instance().add(node);
                    Monitor monitor = new Monitor(node);
                    Thread t = new Thread(monitor);
                    t.setDaemon(true);
                    t.start();

                    routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(201).end(Json.encodePrettily(new Message(Constants.MSG_OK)));
                } else {
                    routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(400).end(Json.encodePrettily(new Message(Constants.TOO_MANY_NODES)));
                }
            } catch (Exception e) {
                routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(400).end(Json.encodePrettily(new Message(e.getMessage())));
            }
        } else {
            routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_TYPE).setStatusCode(400).end(Json.encodePrettily(validations));
        }
    }
}