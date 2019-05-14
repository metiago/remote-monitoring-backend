package io.tiago.player.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorX extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorX.class);

    private static final int PORT = 8000;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MonitorX());
        LOGGER.info("Monitor running listen on port:" + PORT);
    }

    @Override
    public void start() {

        Router router = Router.router(vertx);

        router.route("/assets/*").handler(StaticHandler.create("assets"));

        router.route().handler(BodyHandler.create());

        router.get("/").handler(this::getAll);
        router.get("/:id").handler(this::getOne);
        router.post("/").handler(this::addOne);
        router.put("/:id").handler(this::edit);
        router.delete("/:id").handler(this::deleteOne);

        vertx.createHttpServer().requestHandler(router).listen(PORT);
    }


    private void getAll(RoutingContext routingContext) {


    }

    private void addOne(RoutingContext routingContext) {


    }

    private void getOne(RoutingContext routingContext) {


    }

    private void edit(RoutingContext routingContext) {


    }

    private void deleteOne(RoutingContext routingContext) {


    }
}
