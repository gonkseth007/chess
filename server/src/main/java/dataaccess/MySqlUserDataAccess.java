package dataaccess;

import com.google.gson.Gson;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySqlUserDataAccess implements UserDataAccess {
    public MySqlUserDataAccess() {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertUser(UserData u) throws DataAccessException, SQLException {
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        var json = new Gson().toJson(u);
        executeUpdate(statement, u.username(), u.password(), u.email(), json);
    }

    public UserData getUser(String username) throws DataAccessException, SQLException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, json FROM pet WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        var json = rs.getString("json");
                        return new Gson().fromJson(json, UserData.class);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException();
        }
        return null;
    }

    public void deleteAllUsers() throws DataAccessException, SQLException {
        var statement = "TRUNCATE user";
        executeUpdate(statement);

    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var json = rs.getString("json");
        var user = new Gson().fromJson(json, UserData.class);
        return user;
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException, SQLException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i+1, p);
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS user (
            'username' varchar(256) NOT NULL,
            'password' varchar(256) NOT NULL,
            'email' varchar(256) NOT NULL,
            PRIMARY KEY (username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=ut8mb4_0900_ai_ci
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
