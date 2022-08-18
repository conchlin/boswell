package net.server.rest;

import client.MapleCharacter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.server.Server;
import net.server.world.World;
import scripting.reactor.ReactorScriptManager;
import server.life.MapleMonsterInformationProvider;
import tools.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpServer {
    private static final String HOSTNAME = "127.0.0.1";
    private static final int PORT = 17000;
    private static final int BACKLOG = 1;

    private static final String HEADER_ALLOW = "Allow";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final int STATUS_METHOD_NOT_ALLOWED = 405;

    private static final int NO_RESPONSE_LENGTH = -1;

    public interface RouteInterface {
        Pair<Integer, String> handle(String data);
    }

    public static class Route implements HttpHandler {

        RouteInterface handler;

        public Route(RouteInterface handler) {
            this.handler = handler;
        }

        @Override
        public void handle(HttpExchange he) throws IOException {
            try {
                final Headers headers = he.getResponseHeaders();
                final String requestMethod = he.getRequestMethod().toUpperCase();
                if ("POST".equals(requestMethod)) {
                    InputStream is = he.getRequestBody();
                    String data = "";
                    if (is.available() > 0) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[2048];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                        bos.close();
                        data = new String(bos.toByteArray(), CHARSET);
                    }

                    Pair<Integer, String> response = handler.handle(data);

                    headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));
                    final byte[] rawResponseBody = response.getRight().getBytes(CHARSET);
                    he.sendResponseHeaders(response.getLeft(), rawResponseBody.length);
                    he.getResponseBody().write(rawResponseBody);
                } else {
                    headers.set(HEADER_ALLOW, "POST");
                    he.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                }
            } finally {
                he.close();
            }
        }
    }

    public HttpServer() {
        try {
            final com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), BACKLOG);
            server.createContext("/reloaddrops", new Route(data -> {
                MapleMonsterInformationProvider.getInstance().clearDrops();
                ReactorScriptManager.getInstance().clearDrops();
                return new Pair<>(200, "{\"success\":true}");
            }));
            server.createContext("/saveall", new Route(data -> {
                for (World world : Server.getInstance().getWorlds()) {
                    for (MapleCharacter chr : world.getPlayerStorage().getAllCharacters()) {
                        chr.saveCharToDB();
                    }
                }
                return new Pair<>(200, "{\"success\":true}");
            }));
            server.createContext("/givenx", new Route(data_str -> {
                JsonObject data = JsonParser.parseString(data_str).getAsJsonObject();

                if (data.has("id")) {
                    for (World world : Server.getInstance().getWorlds()) {
                        for (MapleCharacter chr : world.getPlayerStorage().getAllCharacters()) {
                            if (chr.getAccountID() == data.get("id").getAsInt()) {
                                if (data.has("amount")) {
                                    chr.getCashShop().gainCash(1, data.get("amount").getAsInt());
                                    if (chr.getCashShop().getCash(1) < 0) {
                                        chr.getCashShop().gainCash(1, -data.get("amount").getAsInt());
                                        return new Pair<>(200, "{\"success\":false, \"online\":true}");
                                    }
                                } else {
                                    chr.getCashShop().gainCash(1, 4000);
                                }
                                return new Pair<>(200, "{\"success\":true, \"online\":true}");
                            }
                        }
                    }
                    return new Pair<>(200, "{\"success\":true, \"online\":false}");
                }
                return new Pair<>(200, "{\"success\":false, \"online\":false}");
            }));

            server.start();
            System.out.println("http server started port " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
