package dataaccess;

import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlUserDataAccess implements UserDataAccess {
    private final MySqlDataAccessHelper mySqlHelper = new MySqlDataAccessHelper();
    public MySqlUserDataAccess() {
        try {
            mySqlHelper.configureDatabase(createStatements);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertUser(UserData u) throws DataAccessException {
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        mySqlHelper.executeUpdate(statement, u.username(), u.password(), u.email());
    }

    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM users WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException();
        }
        return null;
    }

    public void deleteAllUsers() throws DataAccessException {
        var statement = "TRUNCATE users";
        mySqlHelper.executeUpdate(statement);
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(username, password, email);
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                username varchar(256) NOT NULL,
                password varchar(256) NOT NULL,
                email varchar(256) NOT NULL,
                PRIMARY KEY(username)
            )
            """
    };
}
