package handler;

import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.ClearService;

public class ClearHandler implements Handler {
    ClearService service;
    public ClearHandler(ClearService cService) {
        this.service = cService;
    }

    public void handle(Context context) throws DataAccessException {
        String token = context.header("authorization");
        service.clearDatabase(token);
        context.status(200);
        context.contentType("application/json");
        context.result(String.valueOf(new JsonObject()));
    }
}
