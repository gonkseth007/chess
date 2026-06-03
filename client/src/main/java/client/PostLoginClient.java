package client;

import client.websocket.NotificationHandler;
import model.*;
import webSocketMessages.Notification;

import java.util.*;

import static ui.EscapeSequences.*;

//import client.websocket.WebSocketFacade;

public class PostLoginClient implements NotificationHandler {
    private String visitorName = null;
    private final ServerFacade server;
    private final String serverURL;
    private final String authToken;
    private final HashMap<Integer, GameData> gameDataHashMap = new HashMap<>();
    private Boolean joinedGame = false;
    private ArrayList<String> gameInfo = new ArrayList<>();
//    private final WebSocketFacade ws;
//    private State state = State.SIGNEDOUT;

    public PostLoginClient(String serverURL, String authToken) throws ResponseException {
        server = new ServerFacade(serverURL);
        this.serverURL = serverURL;
        this.authToken = authToken;
//        ws = new WebSocketFacade(serverUrl, this);
    }

    public void run() {
        System.out.println("You have successfully logged in! You can now join, create, or observe games! \nIf you need help, type \"help\" to get some help");

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("logout")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
//                System.out.println("result gotten and it below!");
                System.out.println(SET_TEXT_COLOR_BLUE + result);
                if (joinedGame) {
                    new GameClient(serverURL, authToken, Integer.parseInt(gameInfo.get(0)), Boolean.valueOf(gameInfo.get(1)), gameInfo.get(2)).run();
                    joinedGame = false;
                    gameInfo.clear();
                }

            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public void notify(Notification notification) {
        System.out.println(SET_TEXT_COLOR_RED + notification.message());
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "create", "c" -> createGame(params);
                case "list", "g" -> listGames();
                case "join", "j" -> joinGame(params);
                case "observe", "o" -> observeGame(params);
                case "logout", "l" -> "logout";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String createGame(String... params) throws ResponseException {
        if (params.length >= 1) {
//            state = State.SIGNEDIN;
//            visitorName = String.join("-", params);
//            ws.enterPetShop(visitorName);
            try {
//                System.out.println(params[0]);
//                System.out.println(params[1]);
//                System.out.println(params[2]);
                server.createGame(new CreateGameRequest(params[0], authToken));
            } catch (ResponseException ex) {
//                System.out.println("in the catch");
//                ex.printStackTrace();
                throw new ResponseException();
            }
            return "You have successfully created the game!";
        }
        throw new ResponseException();
    }

    public String joinGame(String... params) throws ResponseException {
        if (params.length > 1) {
//            state = State.SIGNEDIN;
//            visitorName = String.join("-", params);
//            ws.enterPetShop(visitorName);
            GameData game = gameDataHashMap.get(Integer.parseInt(params[0]));
            try {
                server.joinGame(new JoinGameRequest(params[1].toUpperCase(), game.gameID(), authToken));
                gameInfo.addFirst(String.valueOf(game.gameID()));
                gameInfo.add(1, "true");
                gameInfo.add(2, params[1].toUpperCase());
                joinedGame = true;
            } catch (ResponseException ex) {
                throw new ResponseException();
            }
            return String.format("You have successfully joined game #%s - %s!", params[0], game.gameName());
        }
        return "Sorry that input was invalid. To join a game: type \"j\", \"join\" <ID> [WHITE|BLACK]! You must specify either WHITE or BLACK as your player color and what game number you want to join!";
    }

    public String observeGame(String... params) throws ResponseException {
        if (params.length >= 1) {
//            state = State.SIGNEDIN;
//            visitorName = String.join("-", params);
//            ws.enterPetShop(visitorName);
//            try {
//                server.joinGame(new JoinGameRequest(params[1], gameIDs.get(Integer.parseInt(params[0])), authToken));
//            } catch (ResponseException ex) {
//                throw new ResponseException();
//            }
            GameData game = gameDataHashMap.get(Integer.parseInt(params[0]));
            gameInfo.addFirst(String.valueOf(game.gameID()));
            gameInfo.add(1, "false");
            gameInfo.add(2, null);
            joinedGame = true;
            return String.format("You are successfully observing game #%s!", params[0]);
        }
        return "Sorry, to observe a game you must specify which game number you want to observe - list the games again if you don't know the number!";
    }

    public String listGames() throws ResponseException {
//            state = State.SIGNEDIN;
//            visitorName = String.join("-", params);
//            ws.enterPetShop(visitorName);
        StringBuilder listedGamesDisplay = new StringBuilder();
        try {
            ListGamesResult result = server.listGames(authToken);
            Collection<GameData> games = result.games();
            int i = 1;
            for (GameData game : games) {
                listedGamesDisplay.append(String.format("%d. %s\n", i, game.gameName()));
                gameDataHashMap.put(i, game);
                i++;
            }
        } catch (ResponseException ex) {
            throw new ResponseException();
        }
        return listedGamesDisplay.toString();
    }

    public String help() {
        return """
                Options:
                - Logout: "l", "logout"
                - Create a new game: "c", "create" <NAME>
                - List all the games: "g", "list"
                - Join to play a game: "j", "join" <ID> [WHITE|BLACK]
                - Observe a game: "o", "observe" <ID>
                - Get help (this message): "h", "help"
                """;
    }
}
