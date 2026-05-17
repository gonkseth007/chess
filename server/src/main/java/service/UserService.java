package service;

import dataaccess.UserDataAccess;
import dataaccess.AuthDataAccess;
import model.*;
import dataaccess.DataAccessException;

import java.util.UUID;

public class UserService {
    private final UserDataAccess uDataAccess;
    private final AuthDataAccess aDataAccess;

    public UserService(UserDataAccess user, AuthDataAccess auth) {
        this.uDataAccess = user;
        this.aDataAccess = auth;
    }

    public RegisterLoginResult register(RegisterRequest registerRequest) throws DataAccessException {
        uDataAccess.insertUser(new UserData(
                registerRequest.username(),
                registerRequest.password(),
                registerRequest.email()));
        String token = generateAuthToken();
        aDataAccess.insertAuth(new AuthData(
                registerRequest.username(),
                token
        ));
        return new RegisterLoginResult(registerRequest.username(), token);
    }

    public RegisterLoginResult login(LoginRequest loginRequest) throws DataAccessException {
        UserData user = uDataAccess.getUser(loginRequest.username());
        String token = generateAuthToken();
        aDataAccess.insertAuth(new AuthData(
                loginRequest.username(),
                token
        ));
        return new RegisterLoginResult(user.username(), token);
    }

    public void logout(String token) throws DataAccessException {
        AuthData auth = aDataAccess.getAuth(token);
        aDataAccess.deleteAuth(auth);
    }

    public static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
//    public LoginResult login(LoginRequest loginRequest) {}
//    public void logout(LogoutRequest logoutRequest) {}
}