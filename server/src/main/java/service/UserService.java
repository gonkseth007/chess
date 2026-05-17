package service;

import dataaccess.UserDataAccess;
import dataaccess.AuthDataAccess;
import model.*;
import dataaccess.DataAccessException;

import java.util.UUID;

import static service.AuthService.generateAuthToken;

public class UserService {
    private final UserDataAccess uDataAccess;
    private final AuthDataAccess aDataAccess;

    public UserService(UserDataAccess user, AuthDataAccess auth) {
        this.uDataAccess = user;
        this.aDataAccess = auth;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        uDataAccess.insertUser(new UserData(
                registerRequest.username(),
                registerRequest.password(),
                registerRequest.email()));
        String token = generateAuthToken();
        aDataAccess.insertAuth(new AuthData(
                registerRequest.username(),
                token
        ));
        return new RegisterResult(registerRequest.username(), token);
    }

    public static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
//    public LoginResult login(LoginRequest loginRequest) {}
//    public void logout(LogoutRequest logoutRequest) {}
}