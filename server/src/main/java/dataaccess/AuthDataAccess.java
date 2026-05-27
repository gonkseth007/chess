package dataaccess;

import model.AuthData;



public interface AuthDataAccess {
    void insertAuth(AuthData u) throws DataAccessException;

    AuthData getAuth(String token) throws DataAccessException;

    void deleteAuth(AuthData auth) throws DataAccessException;

    void deleteAllAuths() throws DataAccessException;
}
