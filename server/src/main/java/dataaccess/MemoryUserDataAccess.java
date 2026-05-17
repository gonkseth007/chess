package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDataAccess implements UserDataAccess {
    final private HashMap<String, UserData> users = new HashMap<>();

    public void insertUser(UserData user) {
        users.put(user.username(), user);
    }

    public UserData getUser(String username) {
        return users.get(username);
    }

    public void deleteAllUsers() {
        users.clear();
    }
}
