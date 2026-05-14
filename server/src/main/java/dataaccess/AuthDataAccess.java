package dataaccess;

import model.AuthData;
import model.UserData;

public interface AuthDataAccess {
    void insertAuth(AuthData u) throws DataAccessException;

    AuthData getAuth(Integer token) throws DataAccessException;
}
