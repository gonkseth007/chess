package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ClearServiceTest {
    private static ClearService service;
    private static UserDataAccess userDAO;
    private static AuthDataAccess authDAO;
    private static GameDataAccess gameDAO;

    @BeforeAll
    public static void init() {
        userDAO = new MemoryUserDataAccess();
        authDAO = new MemoryAuthDataAccess();
        gameDAO = new MemoryGameDataAccess();
        service = new ClearService(authDAO, userDAO, gameDAO);
    }

    @Test
    @DisplayName("Successful Clear Database")
    void clearDatabase() throws DataAccessException, SQLException {
        userDAO.insertUser(new UserData("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        authDAO.insertAuth(new AuthData("gonkdroid007", "12345"));
        gameDAO.createGame("Gonk's Game");
        gameDAO.createGame("James Bond's Game");
        gameDAO.createGame("Chess Battle");
        service.clearDatabase();
        assertNull(userDAO.getUser("gonkdroid007"));
        assertNull(authDAO.getAuth("12345"));
        assertEquals(0, gameDAO.getAllGames().size());

    }
}