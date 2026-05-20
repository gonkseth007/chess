package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDataAccess {
    GameData createGame(String gameName) throws DataAccessException;

    GameData getGame(Integer gameID) throws DataAccessException;

    public Collection<GameData> getAllGames() throws DataAccessException;

    void updateGame(GameData game) throws DataAccessException;

    void deleteGame(GameData data) throws DataAccessException;

    void deleteAllGames() throws DataAccessException;
}
