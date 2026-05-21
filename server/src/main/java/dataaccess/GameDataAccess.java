package dataaccess;

import model.GameData;

import java.util.List;

public interface GameDataAccess {
    GameData createGame(String gameName) throws DataAccessException;

    GameData getGame(Integer gameID) throws DataAccessException;

    List<GameData> getAllGames() throws DataAccessException;

    void updateGame(GameData game) throws DataAccessException;

    void deleteAllGames() throws DataAccessException;
}
