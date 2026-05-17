package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashMap;

public class MemoryGameDataAccess implements GameDataAccess {
    private int nextId = 1;
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public GameData createGame(String gameName) {
        GameData game = new GameData(nextId, null, null, gameName, new ChessGame());
        games.put(nextId++, game);
        return game;
    }

    public GameData getGame(Integer gameID) {
        return games.get(gameID);
    }

    public void deleteGame(GameData data) {

    }

    public void deleteAllGames() {
        games.clear();
    }
}
