package httpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {

    private List<Route> routes = new ArrayList<>(); 
    private List<Map<String, String>> headers = new ArrayList<>();
    private InetAddress address = InetAddress.getLoopbackAddress();
    private int port = 8080;

    public Server() {}

    public Server(int port) {
        this.port = port;
    }

    public Server(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public Server(InetAddress address, int port, List<Map<String, String>> headers){
        this.headers = headers;
    }

    public Server(int port, List<Map<String, String>> headers){
        this.port = port;
        this.headers = headers;
    }

    public Server(List<Map<String, String>> headers){
        this.headers = headers;
    }

    public void start() throws IOException {
        var server = HttpServer.create(new InetSocketAddress(address, port), 0);
        server.createContext("/", new Handler());
        server.setExecutor(null);
        System.out.println("Server started at address " + server.getAddress());
        server.start();
    }

    class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            handleRequest(t);
        }
    }

    public void registerRoute(String path, Function<Request, Response> handler) {
        var route = new Route(path, "GET", handler);
        routes.add(route);
    }

    public void registerRoute(String path, String method, Function<Request, Response> handler) {
        var route = new Route(path, method, handler);
        routes.add(route);
    }

    public Response notFoundDefaultResponse(Request request) {
        return new Response("Not found", 404);
    }

    void handleRequest(HttpExchange t) {
        var request = new Request(t);
        var response = notFoundDefaultResponse(request);
        for (var route : routes) {
            if (route.matches(request)) {
                response = route.runHandler(request);
                break;
            }
        }
        try {
            t.sendResponseHeaders(response.getStatusCode(), response.getBody().length());
            t.getResponseHeaders().add("Content-Type", response.getContentType());
            for (var header : headers) {
                for (var entry : header.entrySet()) {
                    t.getResponseHeaders().add(entry.getKey(), entry.getValue());
                }
            }
            var os = t.getResponseBody();
            os.write(response.getBody().getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}