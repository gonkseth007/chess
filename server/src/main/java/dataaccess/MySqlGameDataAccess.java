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
        var statement = "INSERT INTO games (gameName, jsonGame) VALUES (?, ?)";
        ChessGame chessGame = new ChessGame();
        int gameID = executeUpdate(statement, gameName, chessGame);
        return new GameData(gameID, null, null, gameName, chessGame);
    }

    public GameData getGame(Integer gameID) throws DataAccessException, SQLException {
        try (var conn = DatabaseManager.getConnection()) {
//            System.out.println("we have gotten connection in getGame");
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, jsonGame FROM games WHERE gameID=?";
            try (var ps = conn.prepareStatement(statement)) {
//                System.out.println("we have prepared the statement");
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
//                    System.out.println("we have executed the query");
                    if (rs.next()) {
//                        System.out.println("we are returning readGame");
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
//                    System.out.println("we addding a game!");
                    allGames.add(getGame(rs.getInt("gameID")));
//                    System.out.println(allGames.getLast().gameID());
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
        executeUpdate(statement, game.whiteUsername(), game.blackUsername(), chessGame, game.gameID());
    }

    public void deleteAllGames() throws DataAccessException, SQLException {
        var statement = "TRUNCATE games";
        executeUpdate(statement);
    }


    private GameData readGame(ResultSet rs) throws SQLException {
//        System.out.println("we are in readGame");
        var json = rs.getString("jsonGame");
//        System.out.println("we have gotten the json");
//        System.out.println(json);
        var chessGame = new Gson().fromJson(json, ChessGame.class);
        int gameID = rs.getInt("gameID");
        String whiteUsername = rs.getString("whiteUsername");
        String blackUsername = rs.getString("blackUsername");
        String gameName = rs.getString("gameName");
//        System.out.print("the id is ");
//        System.out.println(gameName);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException, SQLException {
//        System.out.println("executing this updating");
        try (var conn = DatabaseManager.getConnection()) {
//            System.out.println("got the connection");
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
//                System.out.println("prepared the statement");
                for (var i = 0; i < params.length; i++) {
//                    System.out.println("in the for loop");
                    var param = params[i];
                    if (param instanceof String p) {
//                        System.out.println("if");
                        ps.setString(i + 1, p);
                    }
                    else if (param instanceof ChessGame p) {
//                        System.out.println("else if");
                        ps.setString(i + 1, new Gson().toJson(p));
                    } else if (param == null) ps.setString(i+1, null);
                    else if (param instanceof Integer p) ps.setInt(i+1, p);
                }
//                System.out.println("we about to execute on this statement");
                ps.executeUpdate();
//                System.out.println("we about to generate the keys");
                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
//                    System.out.print("we about to return the ID -> ");
//                    System.out.println(rs.getInt(1));
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException();
        }
    }

    private final String[] createStatements = {
//            """
//            DROP TABLE IF EXISTS games
//            """,
            """
            CREATE TABLE IF NOT EXISTS games (
                gameID int NOT NULL AUTO_INCREMENT,
                whiteUsername varchar(256) DEFAULT NULL,
                blackUsername varchar(256) DEFAULT NULL,
                gameName varchar(256) NOT NULL,
                jsonGame TEXT NOT NULL,
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