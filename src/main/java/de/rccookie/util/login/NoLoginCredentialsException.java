package de.rccookie.util.login;

import java.util.NoSuchElementException;

public class NoLoginCredentialsException extends NoSuchElementException {

    public NoLoginCredentialsException(String host) {
        super("No login entry found for host '" + host + "'. Use the password --host " + host + " -u <username> -p <password> to set login data");
    }
}
