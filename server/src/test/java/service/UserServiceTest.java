package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;



import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private static UserService service;
    private static UserDataAccess userDAO;
    private static AuthDataAccess authDAO;

    @BeforeAll
    public static void init() {
        userDAO = new MemoryUserDataAccess();
        authDAO = new MemoryAuthDataAccess();
        service = new UserService(userDAO, authDAO);
    }

    @BeforeEach
    public void setup() throws DataAccessException {
        new ClearService(authDAO, userDAO, new MemoryGameDataAccess()).clearDatabase();
    }

    @Test
    @DisplayName("Successfully Register User")
    void registerUserSuccess() throws DataAccessException {
        RegisterLoginResult result = service.register(new RegisterRequest("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        assertEquals("gonk@gonk.edu", userDAO.getUser("gonkdroid007").email());
        assertEquals("gonkdroid007", authDAO.getAuth(result.authToken()).username());
    }

    @Test
    @DisplayName("Fail to Register User")
    void registerUserFail() throws DataAccessException {
        service.register(new RegisterRequest("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        assertThrows(AlreadyTakenException.class, () -> service.register(new RegisterRequest("gonkdroid007", "password", "gonkgonk@gonk.edu")));
        assertThrows(BadRequestException.class, () -> service.register(new RegisterRequest(null, "lamepassword", "gonk@gonk.edu")));
    }

    @Test
    @DisplayName("Successfully Login User")
    void loginUserSuccess() throws DataAccessException {
        service.register(new RegisterRequest("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        RegisterLoginResult result = service.login(new LoginRequest("gonkdroid007", "starwarsiscool"));
        assertEquals("gonkdroid007", result.username());
        assertEquals("gonkdroid007", authDAO.getAuth(result.authToken()).username());
    }

    @Test
    @DisplayName("Fail to Login User")
    void loginUserFail() throws DataAccessException {
        service.register(new RegisterRequest("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        assertThrows(AuthorizationException.class, () -> service.login(new LoginRequest("gonkdroid007", "iknowmypasswordtrust")));
        assertThrows(BadRequestException.class, () -> service.login(new LoginRequest(null, "lamepassword")));
    }

    @Test
    @DisplayName("Successfully Logout User")
    void logoutUserSuccess() throws DataAccessException {
        service.register(new RegisterRequest("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        RegisterLoginResult result = service.login(new LoginRequest("gonkdroid007", "starwarsiscool"));
        assertEquals("gonkdroid007", authDAO.getAuth(result.authToken()).username());
        service.logout(result.authToken());
        assertNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    @DisplayName("Fail to Logout User")
    void logoutUserFail() throws DataAccessException {
        service.register(new RegisterRequest("gonkdroid007", "starwarsiscool", "gonk@gonk.edu"));
        assertThrows(AuthorizationException.class, () -> service.logout("thisistotallyarealAuthToken"));
        RegisterLoginResult result = service.login(new LoginRequest("gonkdroid007", "starwarsiscool"));
        assertThrows(AuthorizationException.class, () -> service.logout(String.format("someextra%s", result.authToken())));
    }
}