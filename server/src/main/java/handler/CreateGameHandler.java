package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.CreateGameRequest;
import model.CreateGameResult;
import service.GameService;

import java.sql.SQLException;


public class CreateGameHandler implements Handler {
    GameService service;
    public CreateGameHandler(GameService gService) {
        this.service = gService;
    }

    public void handle(Context context) throws DataAccessException, SQLException {
        String body = context.body();
        String token = context.header("authorization");
        Gson gson = new Gson();
        CreateGameRequest request = gson.fromJson(body, CreateGameRequest.class);
        request = new CreateGameRequest(request.gameName(), token);
        CreateGameResult result = service.createGame(request);
        String res = gson.toJson(result);

        context.status(200);
        context.contentType("application/json");
        context.result(res);
    }
}
