package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    private static GameService service;
    private static GameDataAccess gameDAO;
    private static AuthDataAccess authDAO;

    @BeforeAll
    public static void init() {
        gameDAO = new MemoryGameDataAccess();
        authDAO = new MemoryAuthDataAccess();
        service = new GameService(authDAO, gameDAO);
    }

    @BeforeEach
    public void setup() throws DataAccessException, SQLException {
        new ClearService(authDAO, new MemoryUserDataAccess(), gameDAO).clearDatabase();
        authDAO.insertAuth(new AuthData("gonkgonk", "auth1234"));
        authDAO.insertAuth(new AuthData("jamesbond", "auth5678"));
    }

    @Test
    @DisplayName("Successfully Create Game")
    void createGameSuccess() throws DataAccessException {
        CreateGameResult result = service.createGame(new CreateGameRequest("Cool Game", "auth1234"));
        assertEquals(1, result.gameID());
        assertEquals("Cool Game", gameDAO.getGame(result.gameID()).gameName());
        assertNull(gameDAO.getGame(result.gameID()).whiteUsername());
        assertNull(gameDAO.getGame(result.gameID()).blackUsername());
    }

    @Test
    @DisplayName("Fail to Create Game")
    void createGameFail() {
        assertThrows(AuthorizationException.class, () -> service.createGame(new CreateGameRequest("COOL GAME", "fakeAuth123")));
        assertThrows(BadRequestException.class, () -> service.createGame(new CreateGameRequest(null, "auth1234")));
    }

    @Test
    @DisplayName("Successfully Join Game")
    void joinGameSuccess() throws DataAccessException {
        CreateGameResult result = service.createGame(new CreateGameRequest("Cool Game", "auth1234"));
        service.joinGame(new JoinGameRequest("WHITE", result.gameID(), "auth1234"));
        assertEquals("gonkgonk", gameDAO.getGame(result.gameID()).whiteUsername());
        assertNull(gameDAO.getGame(result.gameID()).blackUsername());
    }

    @Test
    @DisplayName("Fail to Join Game")
    void joinGameFail() throws DataAccessException {
        CreateGameResult result = service.createGame(new CreateGameRequest("Cool Game", "auth1234"));
        service.joinGame(new JoinGameRequest("WHITE", result.gameID(), "auth5678"));
        assertThrows(AuthorizationException.class, () -> service.joinGame(new JoinGameRequest("BLACK", result.gameID(), "fake_auth_hehehe")));
        assertThrows(BadRequestException.class, () -> service.joinGame(new JoinGameRequest("GRAY", result.gameID(), "auth1234")));
        assertThrows(AlreadyTakenException.class, () -> service.joinGame(new JoinGameRequest("WHITE", result.gameID(), "auth1234")));
    }

    @Test
    @DisplayName("Successfully List Game")
    void listGamesSuccess() throws DataAccessException {
        CreateGameResult game1 = service.createGame(new CreateGameRequest("Cool Game", "auth1234"));
        CreateGameResult game2 = service.createGame(new CreateGameRequest("Lame Game", "auth5678"));

        ListGamesResult games = service.listGames("auth1234");
        assertEquals("Cool Game", games.games().getFirst().gameName());
        assertEquals(2, games.games().get(1).gameID());
        assertNotEquals(game1, game2);
    }

    @Test
    @DisplayName("Fail to List Game")
    void listGamesFail() {
        assertThrows(AuthorizationException.class, () -> service.listGames("fake_auth_hehehe"));
    }
}