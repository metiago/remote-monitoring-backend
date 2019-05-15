package io.tiago.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class Server extends AbstractVerticle {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private static final int APP_PORT = 8001;

    private static final String CONTENT_TYPE = "content-type";

    private static final String APPLICATION_TYPE = "application/json; charset=utf-8";

    private static final String MSG_OK = "Data has been saved successfully";

    private static final String MSG_BAD_REQUEST = "Invalid request body";

    private static final String BODY_NOT_EMPTY = "Body must be not empty";

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Server());
        LOGGER.info("Monitor running listen on port:" + APP_PORT);
    }

    @Override
    public void start() {

        Router router = Router.router(vertx);
        router.route("/assets/*").handler(StaticHandler.create("assets"));
        router.route().handler(BodyHandler.create());

        HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create();

        router.get("/").handler(this::getAll);
        //router.get("/:id").handler(this::getOne);
        router.post("/").handler(this::addOne).handler(validationHandler);
        //router.put("/:id").handler(this::edit);
        router.delete("/:id").handler(this::deleteOne);

        vertx.createHttpServer().requestHandler(router).listen(APP_PORT);
    }

    private void getAll(RoutingContext routingContext) {
        LOGGER.info("Listing all registered services");
        routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_TYPE).setStatusCode(200).end(MSG_OK);
    }

    private void addOne(RoutingContext routingContext) {

        String body = routingContext.getBodyAsString();

        if ("".equals(body)) {
            routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_TYPE).setStatusCode(400).end(BODY_NOT_EMPTY);
            return;
        }

        Node node;

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new ParameterNamesModule()).registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
            node = mapper.readValue(body, Node.class);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_TYPE).setStatusCode(400).end(MSG_BAD_REQUEST);
            return;
        }

        Map<String, String> validations = new Validator().validate(node);

        if (validations.size() == 0) {

            try {
                LOGGER.info(String.format("Adding node %s", node));
                Monitor monitor = new Monitor(node);
                Thread t = new Thread(monitor);
                t.start();
                routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_TYPE).setStatusCode(201).end(MSG_OK);
            } catch (Exception e) {
                routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_TYPE).setStatusCode(400).end(e.getMessage());
            }

        } else {
            routingContext.response().putHeader(CONTENT_TYPE, APPLICATION_TYPE).setStatusCode(400).end(Json.encodePrettily(validations));
        }
    }

    private void deleteOne(RoutingContext routingContext) {
    }
}