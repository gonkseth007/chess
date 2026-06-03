package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.NotificationHandler;
import model.CreateGameRequest;
import model.GameData;
import model.JoinGameRequest;
import model.ListGamesResult;
import webSocketMessages.Notification;

import java.util.*;

import static ui.EscapeSequences.*;

//import client.websocket.WebSocketFacade;

public class GameClient implements NotificationHandler {
//    private String visitorName = null;
    private final ServerFacade server;
//    private final String serverURL;
    private final String authToken;
    private final Integer gameID;
    private final boolean isPlaying;
    private final String playerColor;
//    private final HashMap<Integer, GameData> gameDataHashMap = new HashMap<>();
//    private final WebSocketFacade ws;
//    private State state = State.SIGNEDOUT;

    public GameClient(String serverURL, String authToken, Integer gameID, Boolean isPlaying, String playerColor) throws ResponseException {
        server = new ServerFacade(serverURL);
//        this.serverURL = serverURL;
        this.authToken = authToken;
        this.gameID = gameID;
        this.isPlaying = isPlaying;
        this.playerColor = playerColor;
//        ws = new WebSocketFacade(serverUrl, this);
    }

    public void run() throws ResponseException {
//        System.out.println("You have successfully logged in! You can now join, create, or observe games! \nIf you need help, type \"help\" to get some help")
        printBoard();
        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
//                System.out.println("result gotten and it below!");
                System.out.println(SET_TEXT_COLOR_BLUE + result);

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
//                case "create", "c" -> createGame(params);
//                case "list", "g" -> listGames();
//                case "join", "j" -> joinGame(params);
//                case "observe", "o" -> observeGame(params);
                case "show", "s" -> printBoard();
                case "quit", "q" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String printBoard() throws ResponseException {
        if (!this.isPlaying || Objects.equals(this.playerColor, "WHITE")) {
            return printWhiteBoard();
//            return printBlackBoard();
        } else {
            return printBlackBoard();
        }
    }

    public String printWhiteBoard() throws ResponseException {
        Collection<GameData> games = server.listGames(authToken).games();
        ChessBoard board = null;
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                board = game.game().getBoard();
            }
        }
        if (board != null) {
            for (int i = 0; i <= 9; i++) {
                for (int j = 0; j <= 9; j++) {
                    if (i == 0 || i == 9) {
                        System.out.print(SET_BG_COLOR_LIGHT_GREY);
                        System.out.print(SET_TEXT_COLOR_BLACK);
                        if (j != 0 && j != 9) {
                            System.out.printf(" %c ", (char)('a' + (j-1)));
                        } else {
                            System.out.printf(" %c ", ' ');
                        }
                    } else if (j == 0 || j == 9) {
                        System.out.print(SET_BG_COLOR_LIGHT_GREY);
                        System.out.print(SET_TEXT_COLOR_BLACK);
                        System.out.printf(" %d ", (9-i));
                    } else {
                        ChessPiece piece = board.getPiece(new ChessPosition(9-i,j));
                        ChessPiece.PieceType type;
                        if (piece == null) {
                            type = null;
                        } else {
                            type = piece.getPieceType();
                            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                                System.out.print(SET_TEXT_COLOR_MAGENTA);
                            } else {
                                System.out.print(SET_TEXT_COLOR_DARK_GREEN);
                            }
                        }
                        if ((i+j) % 2 == 0) {
                            System.out.print(SET_BG_COLOR_WHITE);
                        } else {
                            System.out.print(SET_BG_COLOR_BLACK);
                        }

                        System.out.printf(" %c ", ChessPieceChar(type));
                    }
                    System.out.print(RESET_TEXT_COLOR);
                    System.out.print(RESET_BG_COLOR);
                }
                System.out.println();
            }
        }
        return "";
    }

    public String printBlackBoard() throws ResponseException {
        Collection<GameData> games = server.listGames(authToken).games();
        ChessBoard board = null;
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                board = game.game().getBoard();
            }
        }
        if (board != null) {
            for (int i = 0; i <= 9; i++) {
                for (int j = 0; j <= 9; j++) {
                    if (i == 0 || i == 9) {
                        System.out.print(SET_BG_COLOR_LIGHT_GREY);
                        System.out.print(SET_TEXT_COLOR_BLACK);
                        if (j != 0 && j != 9) {
                            System.out.printf(" %c ", (char)('a' + (8-j)));
                        } else {
                            System.out.printf(" %c ", ' ');
                        }
                    } else if (j == 0 || j == 9) {
                        System.out.print(SET_BG_COLOR_LIGHT_GREY);
                        System.out.print(SET_TEXT_COLOR_BLACK);
                        System.out.printf(" %d ", (i));
                    } else {
                        ChessPiece piece = board.getPiece(new ChessPosition(i,9-j));
                        ChessPiece.PieceType type;
                        if (piece == null) {
                            type = null;
                        } else {
                            type = piece.getPieceType();
                            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                                System.out.print(SET_TEXT_COLOR_MAGENTA);
                            } else {
                                System.out.print(SET_TEXT_COLOR_DARK_GREEN);
                            }
                        }
                        if ((i+j) % 2 == 0) {
                            System.out.print(SET_BG_COLOR_WHITE);
                        } else {
                            System.out.print(SET_BG_COLOR_BLACK);
                        }

                        System.out.printf(" %c ", ChessPieceChar(type));
                    }
                    System.out.print(RESET_TEXT_COLOR);
                    System.out.print(RESET_BG_COLOR);
                }
                System.out.println();
            }
        }
        return "";
    }

    public char ChessPieceChar(ChessPiece.PieceType type) {
        return switch (type) {
            case ChessPiece.PieceType.QUEEN -> 'Q';
            case ChessPiece.PieceType.KING -> 'K';
            case ChessPiece.PieceType.BISHOP -> 'B';
            case ChessPiece.PieceType.KNIGHT -> 'N';
            case ChessPiece.PieceType.ROOK -> 'R';
            case ChessPiece.PieceType.PAWN -> 'P';
            case null -> ' ';
        };
    }

    public String help() {
        return """
                Options:
                - Quit the game: "q", "quit"
                - Get help (this message): "h", "help"
                """;
    }
}
