package service;

import dataaccess.AuthDataAccess;
import dataaccess.DataAccessException;
import dataaccess.GameDataAccess;
import model.CreateGameRequest;
import model.CreateGameResult;
import model.GameData;

public class GameService {

    private final AuthDataAccess aDataAccess;
    private final GameDataAccess gDataAccess;

    public GameService(AuthDataAccess auth, GameDataAccess game) {
        this.aDataAccess = auth;
        this.gDataAccess = game;
    }

    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException {
        aDataAccess.getAuth(request.authToken());
        GameData game = gDataAccess.createGame(request.gameName());
        return new CreateGameResult(game.gameID());
    }
}
