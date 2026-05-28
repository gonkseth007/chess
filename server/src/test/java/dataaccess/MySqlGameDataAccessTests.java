package dataaccess;

import model.GameData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MySqlGameDataAccessTests {
    private static MySqlGameDataAccess games;

    @BeforeAll
    public static void init() {
        games = new MySqlGameDataAccess();
    }

    @BeforeEach
    public void setup() throws DataAccessException {
        games.deleteAllGames();
    }

    @Test
    @DisplayName("Successfully Insert Game")
    void createGameSuccess() throws DataAccessException, SQLException {
        games.createGame("Chess Game!");
        int game2ID = games.createGame("NUMBA 1 CHESS GAME").gameID();
        var conn = DatabaseManager.getConnection();
        var statement = "SELECT COUNT(*) FROM games";
        var ps = conn.prepareStatement(statement);
        var rs = ps.executeQuery();
        rs.next();
        assertEquals(2, rs.getInt(1));
        statement = "SELECT gameName FROM games WHERE gameID=?";
        ps = conn.prepareStatement(statement);
        ps.setInt(1, game2ID);
        rs = ps.executeQuery();
        rs.next();
        assertEquals("NUMBA 1 CHESS GAME", rs.getString("gameName"));
    }

    @Test
    @DisplayName("Fail to Insert Game")
    void createGameFail() {
        assertThrows(DataAccessException.class, () -> games.createGame(null));
    }

    @Test
    @DisplayName("Successfully Get Game")
    void getGameSuccess() throws DataAccessException {
        int gameID = games.createGame("Chess Game!").gameID();
        assertEquals("Chess Game!", games.getGame(gameID).gameName());
    }

    @Test
    @DisplayName("Fail to Get Game")
    void getGameFail() throws DataAccessException {
        int gameID = games.createGame("Chess Game!").gameID();
        assertEquals("Chess Game!", games.getGame(gameID).gameName());
        gameID += 5;
        assertNull(games.getGame(gameID));
    }

    @Test
    @DisplayName("Successfully Get All Games")
    void getAllGamesSuccess() throws DataAccessException {
        games.createGame("Chess Game!");
        games.createGame("NUMBA 1 CHESS GAME");
        List<GameData> gamesList = games.getAllGames();
        assertEquals("NUMBA 1 CHESS GAME", gamesList.get(1).gameName());
        assertEquals(2, gamesList.size());
    }

//    @Test
//    @DisplayName("Fail to Get All Games")
//    void getAllGamesFail() throws DataAccessException {
//        int gameID = games.createGame("Chess Game!").gameID();
//        int gameID2 = games.createGame("NUMBA 1 CHESS GAME").gameID();
//        List<GameData> gamesList = games.getAllGames();
//        assertEquals("Chess Game!", gamesList.getFirst().gameName());
//        assertEquals(2, gamesList.size());
//    }

    @Test
    @DisplayName("Successfully Update Game")
    void updateGameSuccess() throws DataAccessException {
        int gameID = games.createGame("Chess Game!").gameID();
        assertEquals("Chess Game!", games.getGame(gameID).gameName());
        GameData game = games.getGame(gameID);
        games.updateGame(new GameData(gameID, "gonkdroid007", "jamesbond", "Chess Game!", game.game()));
        assertEquals("gonkdroid007", games.getGame(gameID).whiteUsername());
    }

    @Test
    @DisplayName("Fail to Update Game")
    void updateGameFail() throws DataAccessException {
        int gameID = games.createGame("Chess Game!").gameID();
        GameData game = games.getGame(gameID);
        int finalGameID = gameID + 5;
        games.updateGame(new GameData(finalGameID, "gonkdroid007", "jamesbond", "Chess Game!", game.game()));
        assertNull(games.getGame(finalGameID));
        assertThrows(DataAccessException.class, () -> games.updateGame(new GameData(gameID, "gonkdroid007", "jamesbond", "Chess Game!", null)));
    }

    @Test
    @DisplayName("Successfully Clear the games Database")
    void deleteAllGames() throws DataAccessException {
        int gameID = games.createGame("Chess Game!").gameID();
        var game = games.getGame(gameID);
        assertEquals("Chess Game!", game.gameName());
        games.deleteAllGames();
        assertNull(games.getGame(gameID));

    }
}