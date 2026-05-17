package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.CreateGameRequest;
import model.CreateGameResult;
import service.GameService;


public class CreateGameHandler implements Handler {
    GameService service;
    public CreateGameHandler(GameService gService) {
        this.service = gService;
    }

    public void handle(Context context) throws DataAccessException {
        String body = context.body();
        Gson gson = new Gson();
        CreateGameRequest request = gson.fromJson(body, CreateGameRequest.class);
        CreateGameResult result = service.createGame(request);
        String res = gson.toJson(result);

        context.status(200);
        context.contentType("application/json");
        context.result(res);
    }
}
