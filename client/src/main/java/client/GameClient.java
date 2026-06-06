package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import java.util.*;

import static ui.EscapeSequences.*;

public class GameClient {
//    private String visitorName = null;
    private final ServerFacade server;
    private final String authToken;
    private final Integer gameID;
    private final boolean isPlaying;
    private final String playerColor;

    public GameClient(String serverURL, String authToken, Integer gameID, Boolean isPlaying, String playerColor) {
        server = new ServerFacade(serverURL);
        this.authToken = authToken;
        this.gameID = gameID;
        this.isPlaying = isPlaying;
        this.playerColor = playerColor;
    }

    public void run() throws ResponseException {
        printBoard();
        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
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

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    public String eval(String input) throws ResponseException {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            if (isPlaying) {
                return switch (cmd) {
                    case "show", "s" -> printBoard();
                    case "move", "m" -> makeMove();
                    case "highlight", "t" -> highlightMoves();
                    case "leave", "l" -> leaveGame();
                    case "resign", "r" -> resignFromGame();
                    case "help", "h" -> help();
                    default -> "Sorry, that command isn't a real command! If you need help, type in \"help\" or \"h\"";
                };
            } else {
                return switch (cmd) {
                    case "show", "s" -> printBoard();
                    case "leave", "l" -> "quit";
                    case "help", "h" -> observerHelp();
                    default -> "Sorry, that command isn't a real command! If you need help, type in \"help\" or \"h\"";
                };
            }

    }

    public String highlightMoves() {
        return "Here's the legal moves for that piece!";
    }

    public String makeMove() {
        return "You've made that move";
    }

    public String leaveGame() {
        return "quit";
    }

    public String resignFromGame() {
        if (Objects.equals(this.playerColor, "WHITE")) {
            return "You have resigned from the game and black wins!";
        } else {
            return "You have resigned from the game and white wins!";
        }
    }

    public String printBoard() throws ResponseException {
        if (!this.isPlaying || Objects.equals(this.playerColor, "WHITE")) {
            return printWhiteBoard();
        } else {
            return printBlackBoard();
        }
    }

    public String printWhiteBoard() throws ResponseException {
        ChessGame game = getChessGame();
        ChessBoard board = null;
        if (game != null) {
            board = getChessGame().getBoard();
        }
        if (board != null) {
            for (int i = 0; i <= 9; i++) {
                for (int j = 0; j <= 9; j++) {
                    printWhiteBlock(board, i, j);
                }
                System.out.println();
            }
        }
        return "";
    }

    public String printBlackBoard() throws ResponseException {
        ChessGame game = getChessGame();
        ChessBoard board = null;
        if (game != null) {
            board = getChessGame().getBoard();
        }
        if (board != null) {
            for (int i = 0; i <= 9; i++) {
                for (int j = 0; j <= 9; j++) {
                    printBlackBlock(board, i, j);
                }
                System.out.println();
            }
        }
        return "";
    }

    public void printWhiteBlock(ChessBoard board, int i, int j) {
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
            printTile(board, 9-i, j);
        }
        System.out.print(RESET_TEXT_COLOR);
        System.out.print(RESET_BG_COLOR);
    }

    public void printBlackBlock(ChessBoard board, int i, int j) {
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
            printTile(board, i, 9-j);
        }
        System.out.print(RESET_TEXT_COLOR);
        System.out.print(RESET_BG_COLOR);
    }

    public void printTile(ChessBoard board, int i, int j) {
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
        if ((i+j) % 2 != 0) {
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

    public ChessGame getChessGame() throws ResponseException {
        Collection<GameData> games = server.listGames(authToken).games();
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game.game();
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
//                - Display the board again: "s", "show"
    }

    public String observerHelp() {
        return """
                Options:
                - Display the board again: "s", "show"
                - Leave the game: "l", "leave"
                - Get help (this message): "h", "help"
                """;
    }
}
