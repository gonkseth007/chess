package dataaccess;

import model.UserData;



public interface UserDataAccess {
    void insertUser(UserData u) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void deleteAllUsers() throws DataAccessException;
}
