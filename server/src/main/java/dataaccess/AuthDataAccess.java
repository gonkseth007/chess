package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public interface AuthDataAccess {
    void insertAuth(AuthData u) throws DataAccessException, SQLException;

    AuthData getAuth(String token) throws DataAccessException, SQLException;

    void deleteAuth(AuthData auth) throws DataAccessException, SQLException;

    void deleteAllAuths() throws DataAccessException, SQLException;
}
