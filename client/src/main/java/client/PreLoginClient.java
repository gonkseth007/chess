package client;

import java.util.Arrays;
import java.util.Scanner;

import model.*;

import static ui.EscapeSequences.*;

public class PreLoginClient {
//    private String visitorName = null;
    private final ServerFacade server;
    private final String serverURL;
    private String authToken = null;

    public PreLoginClient(String serverURL) {
        server = new ServerFacade(serverURL);
        this.serverURL = serverURL;
    }

    public void run() {
        System.out.println(WHITE_KING + " Welcome to CHESS! Login or Register to start! (or type \"help\" to get some help)");

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.println(result);
                if (authToken != null) {
                    new PostLoginClient(serverURL, authToken).run();
                    authToken = null;
                }
            } catch (Throwable e) {
                System.out.print(SET_TEXT_COLOR_RED);
                System.out.println("Sorry, an unexpected error occurred! Please try again!");
            }
        }
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register", "r" -> register(params);
                case "login", "l" -> login(params);
                case "quit", "q" -> "quit";
                case "help", "h" -> help();
                default -> "Sorry, that command isn't a real command! " +
                        "If you need help, type in \"help\" or \"h\"";
            };
        } catch (ResponseException ex) {
            System.out.print(SET_TEXT_COLOR_RED);
            return "Sorry, an unexpected error occurred! Please try again!";
        }
    }

    public String register(String... params) throws ResponseException {
        if (params.length > 2) {
            try {
                RegisterLoginResult result = server.register(new RegisterRequest(params[0], params[1], params[2]));
                authToken = result.authToken();
            } catch (BadRequestException ex) {
                System.out.print(SET_TEXT_COLOR_RED);
                return "Oops, you forgot something in the register process! " +
                        "In order to register, type \"register\" or \"r\" and then type in " +
                        "your desired username, password, and your email all separated by a space!";
            } catch (AlreadyTakenException ex) {
                System.out.print(SET_TEXT_COLOR_RED);
                return "Aw shucks! You picked such a cool username that someone else already took it! " +
                        "Try to register again with a different username!";
            } catch (ResponseException ex) {
                throw new ResponseException();
            }
            System.out.print(SET_TEXT_COLOR_BLUE);
            return String.format("You are now a registered user with the username %s!", params[0]);
        }
        System.out.print(SET_TEXT_COLOR_RED);
        return "Oops, you forgot something in the register process! " +
                "In order to register, type \"register\" or \"r\" and then type in " +
                "your desired username, password, and your email all separated by a space!";
    }

    public String login(String... params) throws ResponseException {
        if (params.length > 1) {
            try {
                RegisterLoginResult result = server.login(new LoginRequest(params[0], params[1]));
                authToken = result.authToken();
            } catch (BadRequestException ex) {
                System.out.print(SET_TEXT_COLOR_RED);
                return "Oops, your input wasn't right! In order to login, " +
                        "type \"login\" or \"l\" and then type in your username " +
                        "and your password separated by a space!";
            } catch (AuthorizationException ex) {
                System.out.print(SET_TEXT_COLOR_RED);
                return "Oops, either your username or password were incorrect! " +
                        "If you can't remember your info or haven't registered before, register first! " +
                        "Otherwise try logging in again!";
            } catch (ResponseException ex) {
                throw new ResponseException();
            }
            System.out.print(SET_TEXT_COLOR_BLUE);
            return String.format("You signed in as %s.", params[0]);
        }
        System.out.print(SET_TEXT_COLOR_RED);
        return "Oops, you forgot something while logging in! In order to login, " +
                "type \"login\" or \"l\" and then type in your username " +
                "and your password separated by a space!";
    }

    public String help() {
        return """
                Options:
                - Login as a user: "l", "login" <USERNAME> <PASSWORD>
                - Register a new user: "r", "register" <USERNAME> <PASSWORD> <EMAIL>
                - Quit the program: "q", "quit"
                - Get help (this message): "h", "help"
                """;
    }
}
