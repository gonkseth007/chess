package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class MySqlUserDataAccessTest {
    private static MySqlUserDataAccess users;

    @BeforeAll
    public static void init() {
        users = new MySqlUserDataAccess();
    }

    @BeforeEach
    public void setup() throws DataAccessException {
        users.deleteAllUsers();
    }

    @Test
    @DisplayName("Successfully Insert User")
    void insertUserSuccess() throws DataAccessException, SQLException {
        users.insertUser(new UserData("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        users.insertUser(new UserData("jamesbond", "bondjamesbond", "jamesbond@james.bond"));
        var conn = DatabaseManager.getConnection();
        var statement = "SELECT COUNT(*) FROM users";
        var ps = conn.prepareStatement(statement);
        var rs = ps.executeQuery();
        rs.next();
        assertEquals(2, rs.getInt(1));
        statement = "SELECT email FROM users WHERE username=?";
        ps = conn.prepareStatement(statement);
        ps.setString(1, "jamesbond");
        rs = ps.executeQuery();
        rs.next();
        assertEquals("jamesbond@james.bond", rs.getString("email"));
    }

    @Test
    @DisplayName("Fail to Insert User")
    void insertUserFail() {
        assertThrows(DataAccessException.class, () -> users.insertUser(new UserData("gonkdroid007", "starwarsiscool", null)));
        assertThrows(DataAccessException.class, () -> users.insertUser(new UserData(null, "badpassword", "yes")));
    }

    @Test
    @DisplayName("Successfully Get User")
    void getUserSuccess() throws DataAccessException {
        users.insertUser(new UserData("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        assertEquals("gonk@gonk.edu", users.getUser("gonkdroid007").email());
    }

    @Test
    @DisplayName("Fail to Get User")
    void getUserFail() throws DataAccessException {
        users.insertUser(new UserData("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        assertNull(users.getUser("fakeUser"));
    }

    @Test
    @DisplayName("Successfully Clear the Users Database")
    void deleteAllUsers() throws DataAccessException {
        users.insertUser(new UserData("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        var user = users.getUser("gonkdroid007");
        assertEquals("gonk@gonk.edu", user.email());
        users.deleteAllUsers();
        assertNull(users.getUser("gonkdroid007"));

    }
}