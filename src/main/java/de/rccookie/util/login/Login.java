package de.rccookie.util.login;

import java.util.Objects;

import de.rccookie.json.JsonObject;
import de.rccookie.json.JsonSerializable;
import de.rccookie.util.Arguments;

public final class Login implements JsonSerializable {

    public final String username;
    public final String password;

    public Login(String username, String password) {
        this.username = Arguments.checkNull(username, "username");
        this.password = Arguments.checkNull(password, "password");
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Login)) return false;
        Login login = (Login) o;
        return username.equals(login.username) && password.equals(login.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    @Override
    public Object toJson() {
        return new JsonObject(
                "username", username,
                "password", password
        );
    }
}
