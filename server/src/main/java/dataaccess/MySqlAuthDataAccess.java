package dataaccess;

import model.AuthData;

import java.sql.ResultSet;
import java.sql.SQLException;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySqlAuthDataAccess implements AuthDataAccess {
    public MySqlAuthDataAccess() {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public void insertAuth(AuthData auth) throws DataAccessException {
        var statement = "INSERT INTO auths (username, authToken) VALUES (?, ?)";
        executeUpdate(statement, auth.username(), auth.authToken());
    }

    public AuthData getAuth(String token) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, authToken FROM auths WHERE authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, token);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException();
        }
        return null;
    }

    public void deleteAuth(AuthData auth) throws DataAccessException {
        var statement = "DELETE FROM auths WHERE authToken=?";
        executeUpdate(statement, auth.authToken());
    }

    public void deleteAllAuths() throws DataAccessException {
        var statement = "TRUNCATE auths";
        executeUpdate(statement);
    }


    private AuthData readAuth(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var token = rs.getString("authToken");
        return new AuthData(username, token);
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException {
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
            CREATE TABLE IF NOT EXISTS auths (
                username varchar(256) NOT NULL,
                authToken varchar(256) NOT NULL,
                PRIMARY KEY(authToken),
                INDEX(username)
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
