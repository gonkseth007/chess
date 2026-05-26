package handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.RegisterRequest;
import model.RegisterLoginResult;
import service.UserService;

import java.sql.SQLException;

public class RegisterHandler implements Handler {
    UserService service;
    public RegisterHandler(UserService uService) {
        this.service = uService;
    }

    public void handle(Context context) throws DataAccessException, SQLException {
        String body = context.body();
        Gson gson = new Gson();
        RegisterRequest request = gson.fromJson(body, RegisterRequest.class);
        RegisterLoginResult result = service.register(request);
        String res = gson.toJson(result);

        context.status(200);
        context.contentType("application/json");
//        context.header("CS240", "Awesome!");
        context.result(res);
    }
}
