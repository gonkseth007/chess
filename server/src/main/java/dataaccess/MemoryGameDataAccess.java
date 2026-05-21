package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public List<GameData> getAllGames() { return new ArrayList<>(games.values()); }

    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    public void deleteAllGames() {
        games.clear();
        nextId = 1;
    }
}
