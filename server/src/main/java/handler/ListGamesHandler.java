package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.CreateGameRequest;
import model.CreateGameResult;
import model.ListGamesResult;
import service.GameService;


public class ListGamesHandler implements Handler {
    GameService service;
    public ListGamesHandler(GameService gService) {
        this.service = gService;
    }

    public void handle(Context context) throws DataAccessException {
        String token = context.header("authorization");
        ListGamesResult result = service.listGames(token);
        Gson gson = new Gson();
        String res = gson.toJson(result);

        context.status(200);
        context.contentType("application/json");
        context.result(res);
    }
}
