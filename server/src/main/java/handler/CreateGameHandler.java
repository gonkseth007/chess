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
        System.out.println("we in CreateGameHandler");
        String token = context.header("authorization");
        System.out.println(body);
        System.out.print("here is the authToken -> ");
        System.out.println(token);
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
