package dataaccess;

import model.GameData;

import java.util.HashMap;

public class MemoryGameDataAccess implements GameDataAccess {
    private int nextId = 1;
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public void createGame(GameData data) {
        games.put(nextId++, data);
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
