package handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.CreateGameRequest;
import model.CreateGameResult;
import model.JoinGameRequest;
import model.ListGamesResult;
import service.GameService;


public class JoinGameHandler implements Handler {
    GameService service;
    public JoinGameHandler(GameService gService) {
        this.service = gService;
    }

    public void handle(Context context) throws DataAccessException {
        String body = context.body();
        String token = context.header("authorization");
        Gson gson = new Gson();
        JoinGameRequest request = gson.fromJson(body, JoinGameRequest.class);
        request = new JoinGameRequest(request.playerColor(),request.gameID(),token);
        service.joinGame(request);

        context.status(200);
        context.contentType("application/json");
        context.result(String.valueOf(new JsonObject()));
    }
}
