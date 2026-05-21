package service;

import dataaccess.*;

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
        gDataAccess.deleteAllGames();
        aDataAccess.deleteAllAuths();
        uDataAccess.deleteAllUsers();
    }
}
