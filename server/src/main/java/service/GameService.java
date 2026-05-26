package service;

import dataaccess.*;
import model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class GameService {

    private final AuthDataAccess aDataAccess;
    private final GameDataAccess gDataAccess;

    public GameService(AuthDataAccess auth, GameDataAccess game) {
        this.aDataAccess = auth;
        this.gDataAccess = game;
    }

    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException, SQLException {
        if (aDataAccess.getAuth(request.authToken()) == null) {
            throw new AuthorizationException();
        }
        if (request.gameName() == null) {
            throw new BadRequestException();
        }
        GameData game = gDataAccess.createGame(request.gameName());
        return new CreateGameResult(game.gameID());
    }

    public void joinGame(JoinGameRequest request) throws DataAccessException, SQLException {
        AuthData data = aDataAccess.getAuth(request.authToken());
        if (data == null) {
            throw new AuthorizationException();
        }
        GameData game = gDataAccess.getGame(request.gameID());

        if ((!Objects.equals(request.playerColor(), "WHITE") && !Objects.equals(request.playerColor(), "BLACK")) || game == null) {
            throw new BadRequestException();
        }
        if (Objects.equals(request.playerColor(), "WHITE") && game.whiteUsername() == null) {
            GameData newGame = new GameData(game.gameID(),data.username(),game.blackUsername(),game.gameName(),game.game());
            gDataAccess.updateGame(newGame);
        } else if (Objects.equals(request.playerColor(), "BLACK") && game.blackUsername() == null) {
            GameData newGame = new GameData(game.gameID(),game.whiteUsername(),data.username(),game.gameName(),game.game());
            gDataAccess.updateGame(newGame);
        } else {
            throw new AlreadyTakenException();
        }
    }

    public ListGamesResult listGames(String token) throws DataAccessException, SQLException {
        if (aDataAccess.getAuth(token) == null) {
            throw new AuthorizationException();
        }
        List<GameData> games = gDataAccess.getAllGames();
        return new ListGamesResult(games);
    }
}
