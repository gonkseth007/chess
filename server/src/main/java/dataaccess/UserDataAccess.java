package dataaccess;

import model.UserData;

import java.sql.SQLException;

public interface UserDataAccess {
    void insertUser(UserData u) throws DataAccessException, SQLException;

    UserData getUser(String username) throws DataAccessException, SQLException;

    void deleteAllUsers() throws DataAccessException, SQLException;
}
