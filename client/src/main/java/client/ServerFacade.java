package client;

import model.*;

import java.net.http.*;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) { serverUrl = url; }

    public RegisterLoginResult register(RegisterRequest request) { return null; }

    public RegisterLoginResult login(LoginRequest request) { return null; }

    public void logout(String token) { }

    public void joinGame(JoinGameRequest request) { }

    public CreateGameResult createGame(CreateGameRequest request) { return null; }

    public ListGamesResult listGames(String token) { return null; }

    public void clearDatabase() {}
}
