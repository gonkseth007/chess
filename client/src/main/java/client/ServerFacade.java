package client;

import com.google.gson.Gson;
import model.*;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(int port) { serverUrl = String.format("http://localhost:%d", port); }

    public RegisterLoginResult register(RegisterRequest request) throws ResponseException {
        var req = buildRequest("POST", "/user", request);
        var response = sendRequest(req);
        return handleResponse(response, RegisterLoginResult.class);
    }

    public RegisterLoginResult login(LoginRequest request) { return null; }

    public void logout(String token) { }

    public void joinGame(JoinGameRequest request) { }

    public CreateGameResult createGame(CreateGameRequest request) { return null; }

    public ListGamesResult listGames(String token) { return null; }

    public void clearDatabase() throws ResponseException {
        var request = buildRequest("DELETE", "/db", null);
        sendRequest(request);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder().uri(URI.create(serverUrl + path)).method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException();
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (status / 100 != 2) {
            var body = response.body();
//            if (body != null) {
                throw new ResponseException();
//            }

//            throw new ResponseException;
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }
}
