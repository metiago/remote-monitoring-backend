package io.tiago.monitor;

import io.tiago.monitor.domain.Constants;
import io.tiago.monitor.domain.Message;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RunWith(VertxUnitRunner.class)
public class WebSocketTest {

    @Before
    public void setUp() {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Server());
    }

    @Test
    public void when_valid_request_then_web_socket_ok(TestContext context) {

        LocalTime start = LocalTime.now().plus(Duration.ofSeconds(10));
        LocalTime end = LocalTime.now().plus(Duration.ofMinutes(1));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String s = start.format(formatter);
        String e = end.format(formatter);

        Map<String, Object> body = new HashMap<>();
        body.put("pollFrequency", 2);
        body.put("start", s);
        body.put("end", e);
        body.put("expire", 30);
        body.put("host", "localhost");
        body.put("port", 8001);

        final String json = Json.encodePrettily(body);
        final String length = Integer.toString(json.length());

        Async async = context.async();
        Vertx vertx = Vertx.vertx();
        vertx.createHttpClient().post(8001, "localhost", "/")
                .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE)
                .putHeader("content-length", length)
                .handler(response -> {

                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));

                    response.bodyHandler(resp -> {

                        final Message message = Json.decodeValue(resp.toString(), Message.class);
                        context.assertEquals(message.getMessage(), Constants.MSG_OK);

                        HttpClient client = Vertx.vertx().createHttpClient();
                        client.websocket(8001, "localhost", "/", socket -> socket.handler(data -> {
                            context.assertNotNull(data.toString());
                            async.complete();
                        }));
                    });
                })
                .write(json)
                .end();
    }

    @Test
    public void when_given_valid_data_then_ok(TestContext context) {

        Map<String, Object> body = new HashMap<>();
        body.put("pollFrequency", 2);
        body.put("start", "21:00:00");
        body.put("end", "21:02:00");
        body.put("expire", 30);
        body.put("host", "localhost");
        body.put("port", 8001);

        final String json = Json.encodePrettily(body);
        final String length = Integer.toString(json.length());

        Async async = context.async();
        Vertx vertx = Vertx.vertx();
        vertx.createHttpClient().post(8001, "localhost", "/")
                .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE)
                .putHeader("content-length", length)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(resp -> {
                        final Message message = Json.decodeValue(resp.toString(), Message.class);
                        context.assertEquals(message.getMessage(), Constants.MSG_OK);
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }

    @Test
    public void when_given_invalid_data_then_error(TestContext context) {

        Map<String, Object> body = new HashMap<>();
        body.put("pollFrequency", null);
        body.put("start", "");
        body.put("end", "");
        body.put("expire", null);
        body.put("host", "");
        body.put("port", "port");

        final String json = Json.encodePrettily(body);
        final String length = Integer.toString(json.length());

        Async async = context.async();
        Vertx vertx = Vertx.vertx();
        vertx.createHttpClient().post(8001, "localhost", "/")
                .putHeader(HttpHeaders.CONTENT_TYPE, Constants.APPLICATION_TYPE)
                .putHeader("content-length", length)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 400);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(resp -> {
                        final Message message = Json.decodeValue(resp.toString(), Message.class);
                        context.assertEquals(message.getMessage(), Constants.MSG_BAD_REQUEST);
                        async.complete();
                    });
                })
                .write(json)
                .end();
    }
}
