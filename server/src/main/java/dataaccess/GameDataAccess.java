package dataaccess;

import model.GameData;

import java.sql.SQLException;
import java.util.List;

public interface GameDataAccess {
    GameData createGame(String gameName) throws DataAccessException, SQLException;

    GameData getGame(Integer gameID) throws DataAccessException, SQLException;

    List<GameData> getAllGames() throws DataAccessException;

    void updateGame(GameData game) throws DataAccessException, SQLException;

    void deleteAllGames() throws DataAccessException, SQLException;
}
