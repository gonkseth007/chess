package client;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(String.format("http://localhost:%d", port));
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
    @Order(1)
    void registerUserSuccess() throws ResponseException {
        var result = facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        assertEquals("gonkdroid007", result.username());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    @DisplayName("Fail to Register User")
    @Order(2)
    void registerUserFail() throws ResponseException {
        facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        assertThrows(ResponseException.class, () -> facade.register(new RegisterRequest("gonkdroid007", "iforgotoldpasswordsore-registering", "gonk@gonk.droid")));
        assertThrows(ResponseException.class, () -> facade.register(new RegisterRequest(null, "nouserhahaimsoclever", "troll@trolls.edu")));
    }

    @Test
    @DisplayName("Successfully Login User")
    @Order(3)
    void loginUserSuccess() throws ResponseException {
        facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        RegisterLoginResult result = facade.login(new LoginRequest("gonkdroid007", "gonkdroidrules"));
        assertEquals("gonkdroid007", result.username());
        assertTrue(result.authToken().length() > 10);
    }

    @Test
    @DisplayName("Fail to Login User")
    @Order(4)
    void loginUserFail() throws ResponseException {
        facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        assertThrows(ResponseException.class, () -> facade.login(new LoginRequest("gonkdroid007", "iknowmypasswordtrust")));
        assertThrows(ResponseException.class, () -> facade.login(new LoginRequest(null, "lamepassword")));
    }

    @Test
    @DisplayName("Successfully Logout User")
    @Order(5)
    void logoutUserSuccess() throws ResponseException, DataAccessException {
        MySqlAuthDataAccess auths = new MySqlAuthDataAccess();
        facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        RegisterLoginResult result = facade.login(new LoginRequest("gonkdroid007", "gonkdroidrules"));
        assertEquals("gonkdroid007", result.username());
        facade.logout(result.authToken());
        assertNull(auths.getAuth(result.authToken()));
    }

    @Test
    @DisplayName("Fail to Logout User")
    @Order(6)
    void logoutUserFail() throws ResponseException {
        facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        assertThrows(ResponseException.class, () -> facade.logout("thisistotallyarealAuthToken"));
        RegisterLoginResult result = facade.login(new LoginRequest("gonkdroid007", "gonkdroidrules"));
        assertThrows(ResponseException.class, () -> facade.logout(String.format("someextra%s", result.authToken())));
    }

    @Test
    @DisplayName("Successfully Create Game")
    @Order(7)
    void createGameSuccess() throws DataAccessException, ResponseException {
        var userResult = facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        var gameDAO = new MySqlGameDataAccess();
        CreateGameResult result = facade.createGame(new CreateGameRequest("Cool Game", userResult.authToken()));
        assertEquals(1, result.gameID());
        assertEquals("Cool Game", gameDAO.getGame(result.gameID()).gameName());
        assertNull(gameDAO.getGame(result.gameID()).whiteUsername());
        assertNull(gameDAO.getGame(result.gameID()).blackUsername());
    }

    @Test
    @DisplayName("Fail to Create Game")
    @Order(8)
    void createGameFail() {
        assertThrows(ResponseException.class, () -> facade.createGame(new CreateGameRequest("COOL GAME", "fakeAuth123")));
        assertThrows(ResponseException.class, () -> facade.createGame(new CreateGameRequest(null, "auth1234")));
    }

    @Test
    @DisplayName("Successfully Join Game")
    @Order(9)
    void joinGameSuccess() throws DataAccessException, ResponseException {
        var userResult = facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        var gameDAO = new MySqlGameDataAccess();
        CreateGameResult result = facade.createGame(new CreateGameRequest("Cool Game", userResult.authToken()));
        facade.joinGame(new JoinGameRequest("WHITE", result.gameID(), userResult.authToken()));
        assertEquals("gonkdroid007", gameDAO.getGame(result.gameID()).whiteUsername());
        assertNull(gameDAO.getGame(result.gameID()).blackUsername());
    }

    @Test
    @DisplayName("Fail to Join Game")
    @Order(10)
    void joinGameFail() throws ResponseException {
        var userResult = facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        CreateGameResult result = facade.createGame(new CreateGameRequest("Cool Game", userResult.authToken()));
        facade.joinGame(new JoinGameRequest("WHITE", result.gameID(), userResult.authToken()));
        assertThrows(ResponseException.class, () -> facade.joinGame(new JoinGameRequest("BLACK", result.gameID(), "fake_auth_hehehe")));
        assertThrows(ResponseException.class, () -> facade.joinGame(new JoinGameRequest("GRAY", result.gameID(), "auth1234")));
        assertThrows(ResponseException.class, () -> facade.joinGame(new JoinGameRequest("WHITE", result.gameID(), "auth1234")));
    }

    @Test
    @DisplayName("Successfully List Game")
    @Order(11)
    void listGamesSuccess() throws ResponseException {
        var userResult = facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        CreateGameResult game1 = facade.createGame(new CreateGameRequest("Cool Game", userResult.authToken()));
        CreateGameResult game2 = facade.createGame(new CreateGameRequest("Lame Game", userResult.authToken()));

        ListGamesResult games = facade.listGames(userResult.authToken());
        assertEquals("Cool Game", games.games().getFirst().gameName());
        assertEquals(2, games.games().get(1).gameID());
        assertNotEquals(game1, game2);
    }

    @Test
    @DisplayName("Fail to List Game")
    @Order(12)
    void listGamesFail() {
        assertThrows(ResponseException.class, () -> facade.listGames("fake_auth_hehehe"));
    }

    @Test
    @DisplayName("Successful Clear Database")
    @Order(13)
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
