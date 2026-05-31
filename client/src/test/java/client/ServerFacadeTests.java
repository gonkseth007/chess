package client;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @BeforeEach
    public void setup() throws ResponseException {
        facade.clearDatabase();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("Successfully Register User")
    void registerUserSuccess() throws ResponseException {
        var result = facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        assertEquals("gonkdroid007", result.username());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    @DisplayName("Fail to Register User")
    void registerUserFail() throws ResponseException {
        facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        assertThrows(ResponseException.class, () -> facade.register(new RegisterRequest("gonkdroid007", "iforgotoldpasswordsore-registering", "gonk@gonk.droid")));
        assertThrows(ResponseException.class, () -> facade.register(new RegisterRequest(null, "nouserhahaimsoclever", "troll@trolls.edu")));
    }

    @Test
    @DisplayName("Successfully Login User")
    void loginUserSuccess() throws ResponseException {
        facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        RegisterLoginResult result = facade.login(new LoginRequest("gonkdroid007", "gonkdroidrules"));
        assertEquals("gonkdroid007", result.username());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    @DisplayName("Fail to Login User")
    void loginUserFail() throws ResponseException {
        facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        assertThrows(AuthorizationException.class, () -> facade.login(new LoginRequest("gonkdroid007", "iknowmypasswordtrust")));
        assertThrows(BadRequestException.class, () -> facade.login(new LoginRequest(null, "lamepassword")));
    }

    @Test
    @DisplayName("Successfully Logout User")
    void logoutUserSuccess() throws ResponseException, DataAccessException {
        MySqlUserDataAccess users = new MySqlUserDataAccess();
        facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        RegisterLoginResult result = facade.login(new LoginRequest("gonkdroid007", "starwarsiscool"));
        assertEquals("gonkdroid007", result.username());
        facade.logout(result.authToken());
        assertNull(users.getUser("gonkdroid007"));
    }

    @Test
    @DisplayName("Fail to Logout User")
    void logoutUserFail() throws ResponseException {
        facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        assertThrows(AuthorizationException.class, () -> facade.logout("thisistotallyarealAuthToken"));
        RegisterLoginResult result = facade.login(new LoginRequest("gonkdroid007", "starwarsiscool"));
        assertThrows(AuthorizationException.class, () -> facade.logout(String.format("someextra%s", result.authToken())));
    }

    @Test
    @DisplayName("Successful Clear Database")
    void clearDatabase() throws ResponseException, DataAccessException {
        var userDAO = new MySqlUserDataAccess();
        var authDAO = new MySqlAuthDataAccess();
        var gameDAO = new MySqlGameDataAccess();
        userDAO.insertUser(new UserData("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        authDAO.insertAuth(new AuthData("gonkdroid007", "12345"));
        gameDAO.createGame("Gonk's Game");
        gameDAO.createGame("James Bond's Game");
        gameDAO.createGame("Chess Battle");
        facade.clearDatabase();
        assertNull(userDAO.getUser("gonkdroid007"));
        assertNull(authDAO.getAuth("12345"));
        assertEquals(0, gameDAO.getAllGames().size());
    }

}
