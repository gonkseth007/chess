package server;

import com.google.gson.Gson;
import dataaccess.*;
import handler.*;
import io.javalin.*;
import io.javalin.http.Context;
import server.websocket.WebSocketHandler;
import service.ClearService;
import service.GameService;
import service.UserService;

import java.util.Map;

public class Server {
    private final WebSocketHandler webSocketHandler;
    private final Javalin javalin;
    private final UserService uService;
    private final ClearService cService;
    private final GameService gService;

    public Server() {


        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        UserDataAccess userDAO = new MySqlUserDataAccess();
        AuthDataAccess authDAO = new MySqlAuthDataAccess();
        GameDataAccess gameDAO = new MySqlGameDataAccess();
        webSocketHandler = new WebSocketHandler(authDAO, userDAO, gameDAO);
        uService = new UserService(userDAO, authDAO);
        cService = new ClearService(authDAO, userDAO, gameDAO);
        gService = new GameService(authDAO, gameDAO);
        createHandlers();

        javalin.exception(BadRequestException.class, this::badRequestExceptionHandler);
        javalin.exception(AuthorizationException.class, this::authorizationExceptionHandler);
        javalin.exception(AlreadyTakenException.class, this::alreadyTakenExceptionHandler);
        javalin.exception(Exception.class, this::exceptionHandler);
        javalin.error(404, this::notFound);
        javalin.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler);
            ws.onMessage(webSocketHandler);
            ws.onClose(webSocketHandler);
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void createHandlers() {
        javalin.post("/user", new RegisterHandler(uService));
        javalin.post("/session", new LoginHandler(uService));
        javalin.delete("/session", new LogoutHandler(uService));
        javalin.post("/game", new CreateGameHandler(gService));
        javalin.put("/game", new JoinGameHandler(gService));
        javalin.get("/game", new ListGamesHandler(gService));
        javalin.delete("/db", new ClearHandler(cService));
    }

    private void badRequestExceptionHandler(Exception e, Context context) {
        var body = new Gson().toJson(Map.of("message", "Error: bad request"));
        context.status(400);
        context.json(body);
    }

    private void authorizationExceptionHandler(Exception e, Context context) {
        var body = new Gson().toJson(Map.of("message", "Error: unauthorized"));
        context.status(401);
        context.json(body);
    }

    private void alreadyTakenExceptionHandler(Exception e, Context context) {
        var body = new Gson().toJson(Map.of("message", "Error: already taken"));
        context.status(403);
        context.json(body);
    }

    private void exceptionHandler(Exception e, Context context) {
        var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage())));
        context.status(500);
        context.json(body);
    }

    private void notFound(Context context) {
        String msg = String.format("Error: [%s] %s not found", context.method(), context.path());
        var body = new Gson().toJson(Map.of("message", msg));
        context.json(body);
    }
}
