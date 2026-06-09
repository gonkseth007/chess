package client;

import model.*;

import java.util.*;

import static ui.EscapeSequences.*;

public class PostLoginClient {
//    private String visitorName = null;
    private final ServerFacade server;
    private final String serverURL;
    private final String authToken;
    private final String username;
    private final HashMap<Integer, GameData> gameDataHashMap = new HashMap<>();
    private Boolean joinedGame = false;
    private final ArrayList<String> gameInfo = new ArrayList<>();

    public PostLoginClient(String serverURL, String authToken, String username) {
        server = new ServerFacade(serverURL);
        this.serverURL = serverURL;
        this.authToken = authToken;
        this.username = username;
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    public void run() {
        System.out.println("You have successfully logged in! You can now join, create, or observe games! \n" +
                "If you need help, type \"help\" to get some help");

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("logout")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                if (Objects.equals(result, "logout")) {
                    System.out.println(SET_TEXT_COLOR_BLUE + "You have successfully logged out!");
                } else {
                    System.out.println(SET_TEXT_COLOR_BLUE + result);
                }
                if (joinedGame) {
                    new GameClient(serverURL, authToken, username, Integer.parseInt(gameInfo.get(0)), Boolean.valueOf(gameInfo.get(1)), gameInfo.get(2)).run();
                    joinedGame = false;
                    gameInfo.clear();
                }

            } catch (Throwable e) {
                System.out.print(SET_TEXT_COLOR_RED);
                System.out.println("Sorry, an unexpected error occurred! Please try again!");
            }
        }
    }

    public String createGame(String... params) throws ResponseException {
        if (params.length >= 1) {
            try {
                server.createGame(new CreateGameRequest(params[0], authToken));
            } catch (BadRequestException ex) {
                System.out.print(SET_TEXT_COLOR_RED);
                return "Sorry that input was invalid. " +
                        "To create a game: type \"c\", \"create\" <DESIRED GAME NAME>";
            } catch (AuthorizationException ex) {
                throw new AuthorizationException();
            } catch (ResponseException ex) {
                throw new ResponseException();
            }
            return "You have successfully created the game!";
        }
        return "Oops, you didn't input a name for your game! " +
                "To create a game: type \"c\", \"create\" <DESIRED GAME NAME>";
    }

    public String joinGame(String... params) throws ResponseException {
        if (params.length > 1) {
            String gameName;
            try {
                GameData game = gameDataHashMap.get(Integer.parseInt(params[0]));
                if (game == null) {
                    return "Sorry there is no game correlating to that given number! " +
                            "List out the games and use the number in front of the game name to join!";
                }
                gameName = game.gameName();
                server.joinGame(new JoinGameRequest(params[1].toUpperCase(), game.gameID(), authToken));
                gameInfo.addFirst(String.valueOf(game.gameID()));
                gameInfo.add(1, "true");
                gameInfo.add(2, params[1].toUpperCase());
                joinedGame = true;
            } catch (BadRequestException ex) {
                System.out.print(SET_TEXT_COLOR_RED);
                return "Sorry that input was invalid. To join a game: type \"j\", \"join\" <ID> [WHITE|BLACK]! " +
                        "You must specify either WHITE or BLACK as your player color and what game number you want to join!";
            } catch (AuthorizationException ex) {
                throw new AuthorizationException();
            } catch (AlreadyTakenException ex) {
                System.out.print(SET_TEXT_COLOR_RED);
                return String.format("Aw shucks! Someone is already playing as the %s player!",
                        (params[1].substring(0, 1).toUpperCase() + params[1].substring(1).toLowerCase()));
            } catch (ResponseException ex) {
                throw new ResponseException();
            } catch (NumberFormatException ex) {
                return "Sorry, you typed in a word instead of a number! " +
                        "In order to join a game you need to type in its given number shown when you list the games!";
            }
            return String.format("You have successfully joined game #%s - %s!", params[0], gameName);
        }
        return "Sorry that input was invalid. To join a game: type \"j\", \"join\" <ID> [WHITE|BLACK]! " +
                "You must specify either WHITE or BLACK as your player color and what game number you want to join!";
    }

    public String observeGame(String... params) throws ResponseException {
        if (params.length >= 1) {
            try {
                GameData game = gameDataHashMap.get(Integer.parseInt(params[0]));
                if (game == null) {
                    return "Sorry there is no game correlating to that given number! " +
                            "List out the games and use the number in front of the game name to join!";
                }
                gameInfo.addFirst(String.valueOf(game.gameID()));
                gameInfo.add(1, "false");
                gameInfo.add(2, null);
                joinedGame = true;
            } catch (NumberFormatException ex) {
                return "Sorry, you typed in a word instead of a number! " +
                        "In order to observe a game you need to type in its given number shown when you list the games!";
            }
            return String.format("You are successfully observing game #%s!", params[0]);
        }
        return "Sorry, to observe a game you must specify which game number you want to observe " +
                "- list the games again if you don't know the number!";
    }

    public String listGames() throws ResponseException {
        StringBuilder listedGamesDisplay = new StringBuilder();
        try {
            ListGamesResult result = server.listGames(authToken);
            Collection<GameData> games = result.games();
            int i = 1;
            for (GameData game : games) {
                String whitePlayer = game.whiteUsername();
                if (whitePlayer == null) {
                    whitePlayer = "no one";
                }
                String blackPlayer = game.blackUsername();
                if (blackPlayer == null) {
                    blackPlayer = "no one";
                }
                listedGamesDisplay.append(String.format("%d. %s - White Player: %s, Black Player: %s \n", i, game.gameName(), whitePlayer, blackPlayer));
                gameDataHashMap.put(i, game);
                i++;
            }
        } catch (AuthorizationException ex) {
            throw new AuthorizationException();
        } catch (ResponseException ex) {
            throw new ResponseException();
        }
        return listedGamesDisplay.toString();
    }

    public String logout() throws ResponseException {
        try {
            server.logout(authToken);
        } catch (AuthorizationException ex) {
            throw new AuthorizationException();
        } catch (ResponseException ex) {
            throw new ResponseException();
        }
        return "logout";
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

    public String eval(String input) {
        try {
            String[] tokens = input.split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "create", "c" -> createGame(params);
                case "list", "g" -> listGames();
                case "join", "j" -> joinGame(params);
                case "observe", "o" -> observeGame(params);
                case "logout", "l" -> logout();
                case "help", "h" -> help();
                default -> "Sorry, that command isn't a real command! " +
                        "If you need help, type in \"help\" or \"h\"";
            };
        } catch (AuthorizationException ex) {
            System.out.print(SET_TEXT_COLOR_RED);
            return "Sorry, you are not authorized to perform that action! " +
                    "Try logging out and logging back in!";
        } catch (ResponseException ex) {
            System.out.print(SET_TEXT_COLOR_RED);
            return "Sorry, an unexpected error occurred! Please try again!";
        }
    }
}
