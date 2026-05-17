package server;

import com.google.gson.Gson;
import dataaccess.*;
import handler.RegisterHandler;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final UserService uService;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        UserDataAccess userDAO = new MemoryUserDataAccess();
        AuthDataAccess authDAO = new MemoryAuthDataAccess();
        uService = new UserService(userDAO, authDAO);
        createHandlers();

        javalin.exception(Exception.class, this::exceptionHandler);
        javalin.error(404, this::notFound);

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
