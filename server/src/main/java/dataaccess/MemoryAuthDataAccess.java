package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDataAccess implements AuthDataAccess {
    final private HashMap<String, AuthData> auths = new HashMap<>();

    public void insertAuth(AuthData auth) {
        auths.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String token) {
        System.out.println("in memory auth");
        return auths.get(token);
    }

    public void deleteAuth(AuthData auth) { auths.remove(auth.authToken()); }

    public void deleteAllAuths() {
        auths.clear();
    }
}
