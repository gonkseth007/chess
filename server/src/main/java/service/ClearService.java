package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.GameDataAccess;
import dataaccess.UserDataAccess;

public class ClearService {

    private final AuthDataAccess aDataAccess;
    private final UserDataAccess uDataAccess;
    private final GameDataAccess gDataAccess;

    public ClearService(AuthDataAccess auth, UserDataAccess user, GameDataAccess game) {
        this.aDataAccess = auth;
        this.uDataAccess = user;
        this.gDataAccess = game;
    }

    public void clearDatabase() throws DataAccessException {
        aDataAccess.deleteAllAuths();
        uDataAccess.deleteAllUsers();
        gDataAccess.deleteAllGames();
    }
}
