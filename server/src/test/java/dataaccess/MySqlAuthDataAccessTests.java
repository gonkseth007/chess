package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class MySqlAuthDataAccessTests {
    private static MySqlAuthDataAccess auths;

    @BeforeAll
    public static void init() {
        auths = new MySqlAuthDataAccess();
    }

    @BeforeEach
    public void setup() throws DataAccessException {
        auths.deleteAllAuths();
    }

    @Test
    @DisplayName("Successfully Insert Auth")
    void insertAuthSuccess() throws DataAccessException, SQLException {
        auths.insertAuth(new AuthData("gonkdroid007", "thisisarealauth123"));
        auths.insertAuth(new AuthData("jamesbond007", "007authhere"));
        var conn = DatabaseManager.getConnection();
        var statement = "SELECT COUNT(*) FROM auths";
        var ps = conn.prepareStatement(statement);
        var rs = ps.executeQuery();
        rs.next();
        assertEquals(2, rs.getInt(1));
        statement = "SELECT username FROM auths WHERE authToken=?";
        ps = conn.prepareStatement(statement);
        ps.setString(1, "007authhere");
        rs = ps.executeQuery();
        rs.next();
        assertEquals("jamesbond007", rs.getString("username"));
    }

    @Test
    @DisplayName("Fail to Insert Auth")
    void insertAuthFail() {
        assertThrows(DataAccessException.class, () -> auths.insertAuth(new AuthData(null, "thisisarealauth123")));
        assertThrows(DataAccessException.class, () -> auths.insertAuth(new AuthData("gonkdroid007", null)));
    }

    @Test
    @DisplayName("Successfully Get Auth")
    void getAuthSuccess() throws DataAccessException {
        auths.insertAuth(new AuthData("gonkdroid007", "thisisarealauth123"));
        assertEquals("gonkdroid007", auths.getAuth("thisisarealauth123").username());
    }

    @Test
    @DisplayName("Fail to Get Auth")
    void getAuthFail() throws DataAccessException {
        auths.insertAuth(new AuthData("gonkdroid007", "thisisarealauth123"));
        assertNull(auths.getAuth("fakeAuth"));
    }

    @Test
    @DisplayName("Successfully Delete Auth")
    void deleteAuthSuccess() throws DataAccessException {
        auths.insertAuth(new AuthData("gonkdroid007", "thisisarealauth123"));
        var auth = auths.getAuth("thisisarealauth123");
        assertEquals("gonkdroid007", auth.username());
        auths.deleteAuth(new AuthData("gonkdroid007", "thisisarealauth123"));
        assertNull(auths.getAuth("gonkdroid007"));

    }

    @Test
    @DisplayName("Fail to Delete Auth")
    void deleteAuthFail() throws DataAccessException {
        auths.insertAuth(new AuthData("gonkdroid007", "thisisarealauth123"));
        var auth = auths.getAuth("thisisarealauth123");
        auths.deleteAuth(new AuthData("gonkdroid007", "fakeAuth123"));
        assertEquals("gonkdroid007", auth.username());
    }

    @Test
    @DisplayName("Successfully Clear the auths Database")
    void deleteAllAuths() throws DataAccessException {
        auths.insertAuth(new AuthData("gonkdroid007", "thisisarealauth123"));
        var auth = auths.getAuth("thisisarealauth123");
        assertEquals("gonkdroid007", auth.username());
        auths.deleteAllAuths();
        assertNull(auths.getAuth("gonkdroid007"));

    }
}