package client;

import java.util.Arrays;
import java.util.Scanner;

import model.*;
import client.websocket.NotificationHandler;
//import client.websocket.WebSocketFacade;
import webSocketMessages.Notification;

import static ui.EscapeSequences.*;

public class PreLoginClient implements NotificationHandler {
    private String visitorName = null;
    private final ServerFacade server;
    private final String serverURL;
    private String authToken = null;
//    private final WebSocketFacade ws;
//    private State state = State.SIGNEDOUT;

    public PreLoginClient(String serverURL) throws ResponseException {
        server = new ServerFacade(serverURL);
        this.serverURL = serverURL;
//        ws = new WebSocketFacade(serverUrl, this);
    }

    public void run() {
        System.out.println(WHITE_KING + " Welcome to CHESS!!! Login or Register to start! (or type \"help\" to get some help)");

        Scanner scanner = new Scanner(System.in);
        String result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
//                System.out.println("result gotten and it below!");
                System.out.println(SET_TEXT_COLOR_BLUE + result);
                if (authToken != null) new PostLoginClient(serverURL, authToken).run();
                authToken = null;
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
                case "register", "r" -> register(params);
                case "login", "l" -> login(params);
                case "quit", "q" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException {
        if (params.length > 2) {
//            state = State.SIGNEDIN;
//            visitorName = String.join("-", params);
//            ws.enterPetShop(visitorName);
            try {
//                System.out.println(params[0]);
//                System.out.println(params[1]);
//                System.out.println(params[2]);
                RegisterLoginResult result = server.register(new RegisterRequest(params[0], params[1], params[2]));
                authToken = result.authToken();
            } catch (ResponseException ex) {
                System.out.println("in the catch");
//                ex.printStackTrace();
                throw new ResponseException();
            }
//            System.out.println("we have registered the user!");
            return String.format("You are now a registered user with the username %s!", params[0]);
        }
        throw new ResponseException();
    }

    public String login(String... params) throws ResponseException {
        if (params.length > 1) {
//            state = State.SIGNEDIN;
//            visitorName = String.join("-", params);
//            ws.enterPetShop(visitorName);
            try {
                RegisterLoginResult result = server.login(new LoginRequest(params[0], params[1]));
                authToken = result.authToken();
            } catch (ResponseException ex) {
                throw new ResponseException();
            }
            return String.format("You signed in as %s.", params[0]);
        }
        throw new ResponseException();
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
