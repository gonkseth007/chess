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
        System.out.println("we in serverFacade register");
        System.out.println(request);
        var req = buildRequest("POST", "/user", request, null);
        System.out.println("we built the request");
        System.out.println(req);
        var response = sendRequest(req);
        return handleResponse(response, RegisterLoginResult.class);
    }

    public RegisterLoginResult login(LoginRequest request) throws ResponseException {
        var req = buildRequest("POST", "/session", request, null);
        var response = sendRequest(req);
        return handleResponse(response, RegisterLoginResult.class);
    }

    public void logout(String token) throws ResponseException {
        var request = buildRequest("DELETE", "/session", null, token);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public void joinGame(JoinGameRequest request) throws ResponseException {
        var req = buildRequest("PUT", "/game", request, request.authToken());
        var response = sendRequest(req);
        handleResponse(response, null);
    }

    public CreateGameResult createGame(CreateGameRequest request) throws ResponseException {
        System.out.println("we in serverFacade createGame");
        System.out.println(request);
        System.out.println(request.authToken());
        var req = buildRequest("POST", "/game", request, request.authToken());
        System.out.println("we built the request");
        System.out.println(req);
        var response = sendRequest(req);
        System.out.println("we sent the request");
        System.out.println(response);
        CreateGameResult result = handleResponse(response, CreateGameResult.class);
        System.out.println("we handled the response");
        return result;
    }

    public ListGamesResult listGames(String token) throws ResponseException {
        var request = buildRequest("GET", "/game", null, token);
        var response = sendRequest(request);
        return handleResponse(response, ListGamesResult.class);
    }

    public void clearDatabase() throws ResponseException {
        var request = buildRequest("DELETE", "/db", null, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String header) {
        var request = HttpRequest.newBuilder().uri(URI.create(serverUrl + path)).method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        if (header != null) {
            System.out.println("setting the authorization header!");
            request.setHeader("authorization", header);
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
//            var body = response.body();
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
