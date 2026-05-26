package dataaccess;

import com.google.gson.Gson;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySqlUserDataAccess implements UserDataAccess {
    public MySqlUserDataAccess() {
        try {
//            System.out.println("about to configure the database...");
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
            var statement = "SELECT username, json FROM users WHERE username=?";
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
        var password = rs.getString("password");
        var email = rs.getString("email");
//        var json = rs.getString("json");
//        var user = new Gson().fromJson(json, UserData.class);
        return new UserData(username, password, email);
    }

    private void executeUpdate(String statement, Object... params) throws DataAccessException, SQLException {
//        System.out.println("in executeUpdate...");
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
            CREATE TABLE IF NOT EXISTS users (
                username varchar(256) NOT NULL,
                password varchar(256) NOT NULL,
                email varchar(256) NOT NULL,
                PRIMARY KEY(username)
            ) 
            """
    };

    private void configureDatabase() throws DataAccessException {
//        System.out.println("about to create the database...");
        DatabaseManager.createDatabase();
//        System.out.println("Created the database...");
        try (var conn = DatabaseManager.getConnection()) {
//            System.out.println("Got the database connection...");
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
//                    System.out.print("executing the statement: ");
//                    System.out.println(statement);
                    preparedStatement.executeUpdate();
//                    System.out.println("executed the statement...");
                }
            }
//            System.out.println("Created all the statements...");
        } catch (SQLException ex) {
//            System.out.println(ex.getMessage());
            throw new DataAccessException();
        }
    }
}
