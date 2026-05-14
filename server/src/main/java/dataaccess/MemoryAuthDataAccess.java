package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;

public class MemoryAuthDataAccess implements AuthDataAccess {
    private int nextId = 1;
    final private HashMap<Integer, AuthData> auths = new HashMap<>();

    public void insertAuth(AuthData auth) {
        auths.put(nextId++, auth);
    }

    public AuthData getAuth(Integer token) {
        return auths.get(token);
    }
}
