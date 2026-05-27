package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySqlAuthDataAccess implements AuthDataAccess {
    public MySqlAuthDataAccess() {
        try {
//            System.out.println("about to configure the database...");
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public void insertAuth(AuthData auth) throws DataAccessException, SQLException {
        var statement = "SELECT * FROM auths WHERE username=?";
        try (var conn = DatabaseManager.getConnection()) {
            var ps = conn.prepareStatement(statement);
            ps.setString(1, auth.username());
            var rs = ps.executeQuery();
            if (rs.next()) {
                statement = "UPDATE auths SET authToken=? WHERE username=?";
                executeUpdate(statement, auth.authToken(), auth.username());
            } else {
                statement = "INSERT INTO auths (username, authToken) VALUES (?, ?)";
                executeUpdate(statement, auth.username(), auth.authToken());
            }
        }
    }

    public AuthData getAuth(String token) throws DataAccessException, SQLException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, authToken FROM auths WHERE authToken=?";
//            System.out.print("here is the statement -> ");
//            System.out.println(statement);
            try (var ps = conn.prepareStatement(statement)) {
//                System.out.println("we are in the try prepareStatement");
                ps.setString(1, token);
//                System.out.println("we have set the string");
                try (var rs = ps.executeQuery()) {
//                    System.out.println("we are in the try executeQuery");
                    if (rs.next()) {
//                        var json = rs.getString("json");
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException();
        }
        return null;
    }

    public void deleteAuth(AuthData auth) throws DataAccessException, SQLException {
        var statement = "DELETE FROM auths WHERE authToken=?";
        executeUpdate(statement, auth.authToken());
    }

    public void deleteAllAuths() throws DataAccessException, SQLException {
        var statement = "TRUNCATE auths";
        executeUpdate(statement);
    }


    private AuthData readAuth(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
//        System.out.print("here is the username -> ");
//        System.out.println(username);
        var token = rs.getString("authToken");
//        System.out.println(token);
//        var json = rs.getString("json");
//        var user = new Gson().fromJson(json, UserData.class);
        return new AuthData(username, token);
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
            e.printStackTrace();
            throw new DataAccessException();
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS auths (
                username varchar(256) NOT NULL,
                authToken varchar(256) NOT NULL,
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
