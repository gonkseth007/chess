package dataaccess;

import model.GameData;

public interface GameDataAccess {
    GameData createGame(String gameName) throws DataAccessException;

    GameData getGame(Integer gameID) throws DataAccessException;

    void deleteGame(GameData data) throws DataAccessException;

    void deleteAllGames() throws DataAccessException;
}
