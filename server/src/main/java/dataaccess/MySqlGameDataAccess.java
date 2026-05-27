package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySqlGameDataAccess implements GameDataAccess {
    public MySqlGameDataAccess() {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public GameData createGame(String gameName) throws DataAccessException, SQLException {
        var statement = "INSERT INTO games (gameID, gameName, jsonGame) VALUES (?, ?, ?)";
        ChessGame chessGame = new ChessGame();
        var json = new Gson().toJson(chessGame);
        int gameID = executeUpdate(statement, gameName, json);
        return new GameData(gameID, null, null, gameName, chessGame);
    }

    public GameData getGame(Integer gameID) throws DataAccessException, SQLException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT json FROM games WHERE gameID=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException();
        }
        return null;
    }

    public List<GameData> getAllGames() throws DataAccessException {
        List<GameData> allGames = new ArrayList<>();
        var gameIDQuery = "SELECT gameID FROM games";
        try (var conn = DatabaseManager.getConnection()) {
            var st = conn.createStatement();
            try (var rs = st.executeQuery(gameIDQuery)) {
                while (rs.next()) {
                    allGames.add(getGame(rs.getInt("gameID")));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }

        return allGames;
    }

    public void updateGame(GameData game) throws DataAccessException, SQLException {
        var statement = "UPDATE games SET whiteUsername=?, blackUsername=?, jsonGame=? WHERE gameID=?";
        ChessGame chessGame = game.game();
        var json = new Gson().toJson(chessGame);
        executeUpdate(statement, game.whiteUsername(), game.blackUsername(), json);
    }

    public void deleteAllGames() throws DataAccessException, SQLException {
        var statement = "TRUNCATE games";
        executeUpdate(statement);
    }


    private GameData readGame(ResultSet rs) throws SQLException {
        var json = rs.getString("json");
        return new Gson().fromJson(json, GameData.class);
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException, SQLException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i+1, p);
                }
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS games (
                gameID int NOT NULL AUTO_INCREMENT,
                whiteUsername varchar(256) DEFAULT NULL,
                blackUsername varchar(256) DEFAULT NULL,
                gameName varchar(256) NOT NULL,
                jsonGame varchar(256) NOT NULL,
                PRIMARY KEY(gameID)
            )
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException();
        }
    }
}