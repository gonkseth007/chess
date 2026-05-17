package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;

public class MemoryAuthDataAccess implements AuthDataAccess {
//    private int nextId = 1;
    final private HashMap<String, AuthData> auths = new HashMap<>();

    public void insertAuth(AuthData auth) {
        auths.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String token) {
        return auths.get(token);
    }

    public void deleteAuth(AuthData auth) { auths.remove(auth.authToken()); }

    public void deleteAllAuths() {
        auths.clear();
    }
}
