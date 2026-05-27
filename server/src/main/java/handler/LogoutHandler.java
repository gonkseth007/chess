package handler;

import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.UserService;



public class LogoutHandler implements Handler {
    UserService service;
    public LogoutHandler(UserService uService) {
        this.service = uService;
    }

    public void handle(Context context) throws DataAccessException {
        String token = context.header("authorization");
        service.logout(token);
        context.status(200);
        context.contentType("application/json");
        context.result(String.valueOf(new JsonObject()));
    }
}
