package client;

import model.RegisterRequest;
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
    public void registerUserSuccess() throws ResponseException {
        var result = facade.register(new RegisterRequest("gonkdroid007", "gonkdroidrules", "gonk@gonk.gonk"));
        assertEquals("gonkdroid007", result.username());
        assertTrue(result.authToken().length() > 10);
    }

}
