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
        assertThrows(ResponseException.class, () -> facade.login(new LoginRequest("gonkdroid007", "iknowmypasswordtrust")));
        assertThrows(ResponseException.class, () -> facade.login(new LoginRequest(null, "lamepassword")));
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
        assertThrows(ResponseException.class, () -> facade.logout("thisistotallyarealAuthToken"));
        RegisterLoginResult result = facade.login(new LoginRequest("gonkdroid007", "starwarsiscool"));
        assertThrows(ResponseException.class, () -> facade.logout(String.format("someextra%s", result.authToken())));
    }

    @Test
    @DisplayName("Successfully Create Game")
    void createGameSuccess() throws DataAccessException {
        var gameDAO = new MySqlGameDataAccess();
        CreateGameResult result = facade.createGame(new CreateGameRequest("Cool Game", "auth1234"));
        assertEquals(1, result.gameID());
        assertEquals("Cool Game", gameDAO.getGame(result.gameID()).gameName());
        assertNull(gameDAO.getGame(result.gameID()).whiteUsername());
        assertNull(gameDAO.getGame(result.gameID()).blackUsername());
    }

    @Test
    @DisplayName("Fail to Create Game")
    void createGameFail() {
        assertThrows(ResponseException.class, () -> facade.createGame(new CreateGameRequest("COOL GAME", "fakeAuth123")));
        assertThrows(ResponseException.class, () -> facade.createGame(new CreateGameRequest(null, "auth1234")));
    }

    @Test
    @DisplayName("Successfully Join Game")
    void joinGameSuccess() throws DataAccessException {
        var gameDAO = new MySqlGameDataAccess();
        CreateGameResult result = facade.createGame(new CreateGameRequest("Cool Game", "auth1234"));
        facade.joinGame(new JoinGameRequest("WHITE", result.gameID(), "auth1234"));
        assertEquals("gonkgonk", gameDAO.getGame(result.gameID()).whiteUsername());
        assertNull(gameDAO.getGame(result.gameID()).blackUsername());
    }

    @Test
    @DisplayName("Fail to Join Game")
    void joinGameFail() throws ResponseException {
        CreateGameResult result = facade.createGame(new CreateGameRequest("Cool Game", "auth1234"));
        facade.joinGame(new JoinGameRequest("WHITE", result.gameID(), "auth5678"));
        assertThrows(ResponseException.class, () -> facade.joinGame(new JoinGameRequest("BLACK", result.gameID(), "fake_auth_hehehe")));
        assertThrows(ResponseException.class, () -> facade.joinGame(new JoinGameRequest("GRAY", result.gameID(), "auth1234")));
        assertThrows(ResponseException.class, () -> facade.joinGame(new JoinGameRequest("WHITE", result.gameID(), "auth1234")));
    }

    @Test
    @DisplayName("Successfully List Game")
    void listGamesSuccess() {
        CreateGameResult game1 = facade.createGame(new CreateGameRequest("Cool Game", "auth1234"));
        CreateGameResult game2 = facade.createGame(new CreateGameRequest("Lame Game", "auth5678"));

        ListGamesResult games = facade.listGames("auth1234");
        assertEquals("Cool Game", games.games().getFirst().gameName());
        assertEquals(2, games.games().get(1).gameID());
        assertNotEquals(game1, game2);
    }

    @Test
    @DisplayName("Fail to List Game")
    void listGamesFail() {
        assertThrows(ResponseException.class, () -> facade.listGames("fake_auth_hehehe"));
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
