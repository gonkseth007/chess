package client;

import chess.*;
import client.websocket.ServerMessageHandler;
import client.websocket.WebSocketFacade;
import model.GameData;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.*;

import static ui.EscapeSequences.*;

public class GameClient implements ServerMessageHandler {
    private final ServerFacade server;
    private final WebSocketFacade ws;
    private final String authToken;
    private final Integer gameID;
    private final boolean isPlaying;
    private final String playerColor;

    public GameClient(String serverURL, String authToken, Integer gameID, Boolean isPlaying, String playerColor) throws ResponseException {
        server = new ServerFacade(serverURL);
        ws = new WebSocketFacade(serverURL, this);
        this.authToken = authToken;
        this.gameID = gameID;
        this.isPlaying = isPlaying;
        this.playerColor = playerColor;
    }

    public void run() throws ResponseException {
        ws.connectToGame(authToken, gameID);
        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("You have left the game!")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.println(SET_TEXT_COLOR_BLUE + result);

            } catch (Throwable e) {
                System.out.print(SET_TEXT_COLOR_RED);
                System.out.println("Sorry, an unexpected error occurred! Please try again!");
            }
        }
    }

    public void notify(ServerMessage message) throws ResponseException {
        System.out.println();
        if (message.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
            NotificationMessage notification = (NotificationMessage) message;
            System.out.println(SET_TEXT_COLOR_MAGENTA + notification.getMessage());
        } else if (message.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            LoadGameMessage loadedGame = (LoadGameMessage) message;
            printBoard(loadedGame.getGame(), null, null);
        } else {
            ErrorMessage error = (ErrorMessage) message;
            System.out.println(SET_TEXT_COLOR_RED + error.getMessage());
        }
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    public String eval(String input) throws ResponseException {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
//            if (isPlaying) {
                return switch (cmd) {
                    case "show", "s" -> printBoard(getGameData().game(), null, null);
                    case "move", "m" -> makeMove(params);
                    case "highlight", "t" -> highlightMoves(params);
                    case "leave", "l" -> leaveGame();
                    case "resign", "r" -> resignFromGame();
                    case "help", "h" -> help();
                    default -> "Sorry, that command isn't a real command! If you need help, type in \"help\" or \"h\"";
                };
//            } else {
//                return switch (cmd) {
//                    case "show", "s" -> printBoard(getGameData().game());
//                    case "leave", "l" -> leaveGame();
//                    case "help", "h" -> observerHelp();
//                    default -> "Sorry, that command isn't a real command! If you need help, type in \"help\" or \"h\"";
//                };
//            }
    }

    public String highlightMoves(String... params) throws ResponseException {
        if (params.length < 1) {
            return "Oops, you forgot to enter in what position of the piece you want the moves to be highlighted for!";
        }
        if (params[0].length() != 2) {
            return "Oops! That position isn't valid. Valid positions must be inputted with the letter then the number. (e.g. a1 or F6)";
        }
        int x = params[0].charAt(0) - 'a' + 1;
        int y = Character.getNumericValue(params[0].charAt(1));
        if (y < 1 || y > 8 || x < 1 || x > 8) {
            return "Oops! That position isn't valid. Valid positions must be inputted with the letter then the number. (e.g. a1 or F6)";
        }
        ChessGame game = getGameData().game();
        ChessPosition pos = new ChessPosition(y, x);
        ChessPiece piece = game.getBoard().getPiece(pos);
        if (piece == null) {
            return "Oops! There is no chess piece at the entered position!";
        }
        return printBoard(game, piece, pos);
    }

    public String makeMove(String... params) {
        if (params.length > 1) {
            try {
                ws.makeMove(authToken, gameID, params);
                return "move made!";
            } catch (InvalidMovePositionsException ex) {
                return "Error: One or both those positions aren't valid. Valid positions must be inputted with the letter then the number. (e.g. a1 or F6)";
            } catch (InvalidMoveException e) {
                return "Error: That move wasn't valid!";
            } catch (AuthorizationException e) {
                return "Error: You aren't authorized to do that... try logging in again?";
            }

        }
        return "Oops, you forgot something! You must enter in a valid start position and end position (e.g. \"move A2 A3\")";
    }

    public String leaveGame() {
        ws.leaveGame(authToken, gameID);
        return "You have left the game!";
    }

    public String resignFromGame() {
        ws.resignFromGame(authToken, gameID);
        if (Objects.equals(this.playerColor, "WHITE")) {
            return "You have resigned from the game and black wins!";
        } else {
            return "You have resigned from the game and white wins!";
        }
    }

    public String printBoard(ChessGame game, ChessPiece piece, ChessPosition pos) throws ResponseException {
        if (!this.isPlaying || Objects.equals(this.playerColor, "WHITE")) {
            return printWhiteBoard(game, piece, pos);
        } else {
            return printBlackBoard(game, piece, pos);
        }
    }

    public String printWhiteBoard(ChessGame game, ChessPiece piece, ChessPosition pos) {
        ChessBoard board = null;
        if (game != null) {
            board = game.getBoard();
        }
        Collection<ChessMove> moves = null;
        if (piece != null) {
            moves = piece.pieceMoves(board, pos);
        }
        if (board != null) {
            for (int i = 0; i <= 9; i++) {
                for (int j = 0; j <= 9; j++) {
                    printWhiteBlock(board, i, j, moves);
                }
                System.out.println();
            }
        }
        return "";
    }

    public String printBlackBoard(ChessGame game, ChessPiece piece, ChessPosition pos) {
        ChessBoard board = null;
        if (game != null) {
            board = game.getBoard();
        }
        Collection<ChessMove> moves = null;
        if (piece != null) {
            moves = piece.pieceMoves(board, pos);
        }
        if (board != null) {
            for (int i = 0; i <= 9; i++) {
                for (int j = 0; j <= 9; j++) {
                    printBlackBlock(board, i, j, moves);
                }
                System.out.println();
            }
        }
        return "";
    }

    public void printWhiteBlock(ChessBoard board, int i, int j, Collection<ChessMove> moves) {
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
            printTile(board, 9-i, j, moves);
        }
        System.out.print(RESET_TEXT_COLOR);
        System.out.print(RESET_BG_COLOR);
    }

    public void printBlackBlock(ChessBoard board, int i, int j, Collection<ChessMove> moves) {
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
            printTile(board, i, 9-j, moves);
        }
        System.out.print(RESET_TEXT_COLOR);
        System.out.print(RESET_BG_COLOR);
    }

    public void printTile(ChessBoard board, int i, int j, Collection<ChessMove> moves) {
        ChessPiece piece = board.getPiece(new ChessPosition(i,j));
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
        boolean isStartPos = false;
        boolean highlight = false;
        if (moves != null) {
            for (ChessMove move: moves) {
                if (move.getStartPosition().getRow() == i && move.getStartPosition().getColumn() == j) {
                    isStartPos = true;
                }
                break;
            }
            for (ChessMove move : moves) {
                if (move.getEndPosition().getRow() == i && move.getEndPosition().getColumn() == j) {
                    highlight = true;
                    break;
                }
            }
        }
        if (highlight) {
            if ((i+j) % 2 != 0) {
                System.out.print(SET_BG_COLOR_BLUE);
            } else {
                System.out.print(SET_BG_COLOR_DARK_BLUE);
            }
            System.out.print(SET_TEXT_COLOR_WHITE);
        } else if (isStartPos) {
            System.out.print(SET_BG_COLOR_GREEN);
            System.out.print(SET_TEXT_COLOR_BLACK);
        } else if ((i+j) % 2 != 0) {
            System.out.print(SET_BG_COLOR_WHITE);
        } else {
            System.out.print(SET_BG_COLOR_BLACK);
        }

        System.out.printf(" %c ", chessPieceChar(type));
    }

    public char chessPieceChar(ChessPiece.PieceType type) {
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

    public GameData getGameData() throws ResponseException {
        Collection<GameData> games = server.listGames(authToken).games();
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    public String help() {
        return """
                Options:
                - Display the board again: "s", "show"
                - Make a move: "m", "move" <START POSITION> <END POSITION>
                - Highlight legal moves: "t", "highlight" <PIECE POSITION>
                - Resign from the game: "r", "resign"
                - Leave the game: "l", "leave"
                - Get help (this message): "h", "help"
                """;
    }

    public String observerHelp() {
        return """
                Options:
                - Display the board again: "s", "show"
                - Leave the game: "l", "leave"
                - Get help (this message): "h", "help"
                """;
    }

    public String pawnPromotionPrompt() {
        return """
                Please enter in the piece you'd like to promote your pawn to!
                - Queen
                - Rook
                - Knight
                - Bishop
                """;
    }
}
