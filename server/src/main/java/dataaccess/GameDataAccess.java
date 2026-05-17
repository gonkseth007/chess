package dataaccess;

import model.GameData;

public interface GameDataAccess {
    void createGame(GameData data) throws DataAccessException;

    GameData getGame(Integer gameID) throws DataAccessException;

    void deleteGame(GameData data) throws DataAccessException;

    void deleteAllGames() throws DataAccessException;
}
