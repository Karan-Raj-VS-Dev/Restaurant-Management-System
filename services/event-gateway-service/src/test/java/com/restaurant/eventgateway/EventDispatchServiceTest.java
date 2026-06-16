package com.restaurant.eventgateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EventDispatchServiceTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void publishCountsSuccessfulAndFailedSubscribers() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/events", exchange -> {
            exchange.sendResponseHeaders(202, 0);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(new byte[0]);
            }
        });
        server.start();

        int port = server.getAddress().getPort();
        EventDispatchService service = new EventDispatchService(
                "http://127.0.0.1:" + port + "/events,http://127.0.0.1:1/unreachable"
        );

        EventDispatchResponse response = service.publish("{\"eventKey\":\"order.created\"}");

        assertThat(response.delivered()).isEqualTo(1);
        assertThat(response.failed()).isEqualTo(1);
    }

    @Test
    void publishWithNoSubscribersReturnsZeroCounts() {
        EventDispatchService service = new EventDispatchService("");

        EventDispatchResponse response = service.publish("{\"eventKey\":\"order.created\"}");

        assertThat(response.delivered()).isZero();
        assertThat(response.failed()).isZero();
    }

    @Test
    void publishIgnoresBlankSubscribersAndTrimsValidOnes() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/events", exchange -> {
            exchange.sendResponseHeaders(202, 0);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(new byte[0]);
            }
        });
        server.start();

        int port = server.getAddress().getPort();
        EventDispatchService service = new EventDispatchService(
                "   , http://127.0.0.1:" + port + "/events  ,   "
        );

        EventDispatchResponse response = service.publish("{\"eventKey\":\"payment.succeeded\"}");

        assertThat(response.delivered()).isEqualTo(1);
        assertThat(response.failed()).isZero();
    }
}
