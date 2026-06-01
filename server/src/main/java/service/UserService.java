package service;

import dataaccess.*;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {
    private final UserDataAccess uDataAccess;
    private final AuthDataAccess aDataAccess;

    public UserService(UserDataAccess user, AuthDataAccess auth) {
        this.uDataAccess = user;
        this.aDataAccess = auth;
    }

    public RegisterLoginResult register(RegisterRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null || request.email() == null) {
            throw new BadRequestException();
        }
        if (uDataAccess.getUser(request.username()) != null) {
            throw new AlreadyTakenException();
        }
        String hashedPassword = BCrypt.hashpw(request.password(), BCrypt.gensalt());
        uDataAccess.insertUser(new UserData(
                request.username(),
                hashedPassword,
                request.email()));
        String token = generateAuthToken();
        aDataAccess.insertAuth(new AuthData(
                request.username(),
                token
        ));
        return new RegisterLoginResult(request.username(), token);
    }

    public RegisterLoginResult login(LoginRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null) {
            throw new BadRequestException();
        }
        UserData user = uDataAccess.getUser(request.username());
        if (user == null) {
            throw new AuthorizationException();
        }
        if (!BCrypt.checkpw(request.password(), user.password())) {
            throw new AuthorizationException();
        }
        String token = generateAuthToken();
        aDataAccess.insertAuth(new AuthData(
                request.username(),
                token
        ));
        return new RegisterLoginResult(user.username(), token);
    }

    public void logout(String token) throws DataAccessException {
        AuthData auth = aDataAccess.getAuth(token);
        System.out.print("we are in logout and auth is ");
        System.out.println(auth);
        if (auth == null) {
            throw new AuthorizationException();
        }
        aDataAccess.deleteAuth(auth);
    }

    public static String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}