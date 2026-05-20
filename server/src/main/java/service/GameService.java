package service;

import dataaccess.*;
import model.*;

import java.util.Collection;
import java.util.Objects;

public class GameService {

    private final AuthDataAccess aDataAccess;
    private final GameDataAccess gDataAccess;

    public GameService(AuthDataAccess auth, GameDataAccess game) {
        this.aDataAccess = auth;
        this.gDataAccess = game;
    }

    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException {
        if (aDataAccess.getAuth(request.authToken()) == null) {
            throw new AuthorizationException();
        }
        GameData game = gDataAccess.createGame(request.gameName());
        return new CreateGameResult(game.gameID());
    }

    public void joinGame(JoinGameRequest request) throws DataAccessException {
        System.out.print("auth token is ");
        System.out.println(request.authToken());
        AuthData data = aDataAccess.getAuth(request.authToken());
        if (data == null) {
            throw new AuthorizationException();
        }

        GameData game = gDataAccess.getGame(request.gameID());
        if (Objects.equals(request.playerColor(), "WHITE") && game.whiteUsername() == null) {
            GameData newGame = new GameData(game.gameID(),data.username(),game.blackUsername(),game.gameName(),game.game());
            gDataAccess.updateGame(newGame);
        } else if (Objects.equals(request.playerColor(), "BLACK") && game.blackUsername() == null) {
            GameData newGame = new GameData(game.gameID(),game.whiteUsername(),data.username(),game.gameName(),game.game());
            gDataAccess.updateGame(newGame);
        }
//        throw new DataAccessException;
    }

    public ListGamesResult listGames(String token) throws DataAccessException {
        if (aDataAccess.getAuth(token) == null) {
            throw new AuthorizationException();
        }
        Collection<GameData> games = gDataAccess.getAllGames();
        return new ListGamesResult(games);
    }
}
