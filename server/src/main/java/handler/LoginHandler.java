package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.LoginRequest;
import model.RegisterLoginResult;
import service.UserService;

public class LoginHandler implements Handler {
    UserService service;
    public LoginHandler(UserService uService) {
        this.service = uService;
    }

    public void handle(Context context) throws DataAccessException {
        String body = context.body();
        Gson gson = new Gson();
        LoginRequest request = gson.fromJson(body, LoginRequest.class);
        RegisterLoginResult result = service.login(request);
        String res = gson.toJson(result);

        context.status(200);
        context.contentType("application/json");
        context.result(res);
    }
}
