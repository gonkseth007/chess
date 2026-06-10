package dataaccess;

import model.AuthData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlAuthDataAccess implements AuthDataAccess {
    private final MySqlDataAccessHelper mySqlHelper = new MySqlDataAccessHelper();
    public MySqlAuthDataAccess() {
        try {
            mySqlHelper.configureDatabase(createStatements);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public void insertAuth(AuthData auth) throws DataAccessException {
        var statement = "INSERT INTO auths (username, authToken) VALUES (?, ?)";
        mySqlHelper.executeUpdate(statement, auth.username(), auth.authToken());
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
        mySqlHelper.executeUpdate(statement, auth.authToken());
    }

    public void deleteAllAuths() throws DataAccessException {
        var statement = "TRUNCATE auths";
        mySqlHelper.executeUpdate(statement);
    }


    private AuthData readAuth(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var token = rs.getString("authToken");
        return new AuthData(username, token);
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
}
