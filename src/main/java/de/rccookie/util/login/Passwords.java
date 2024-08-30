package de.rccookie.util.login;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.rccookie.json.Json;
import de.rccookie.json.JsonObject;
import de.rccookie.util.Options;
import de.rccookie.util.ArgsParser;
import de.rccookie.util.Arguments;
import de.rccookie.util.Console;
import de.rccookie.util.XORDecodedInputStream;
import de.rccookie.util.XOREncodedOutputStream;

public final class Passwords {

    private Passwords() {
        throw new UnsupportedOperationException();
    }

    private static final Map<String, Login> PASSWORDS = new HashMap<>() {
        @Override
        public String toString() {
            return Json.toString(this);
        }
    };

    static {
        load(false);
    }

    public static Login get(String host) {
        Arguments.checkNull(host, "host");
        if(!PASSWORDS.containsKey(host)) throw new NoLoginCredentialsException(host);
        return PASSWORDS.get(host);
    }

    public static Map<String, Login> getAll() {
        return Collections.unmodifiableMap(PASSWORDS);
    }

    public static void set(String host, String username, String password) {
        set(host, new Login(username, password));
    }

    public static void set(String host, Login login) {
        PASSWORDS.put(Arguments.checkNull(host, "host"), Arguments.checkNull(login, "login"));
        try {
            Json.write(PASSWORDS, new XOREncodedOutputStream(new FileOutputStream(new File(System.getProperty("user.home"), "passwords.json"))));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean remove(String host) {
        boolean success = PASSWORDS.remove(host) != null;
        try {
            Json.write(PASSWORDS, new XOREncodedOutputStream(new FileOutputStream(new File(System.getProperty("user.home"), "passwords.json"))));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
        return success;
    }

    public static void reload() {
        load(true);
    }

    private static void load(boolean throwExceptions) {
        JsonObject passwords;
        try {
            passwords = Json.parse(new XORDecodedInputStream(new FileInputStream(new File(System.getProperty("user.home"), "passwords.json")))).asObject();
        } catch(Exception e) {
            if(!(e instanceof FileNotFoundException)) {
                if(throwExceptions)
                    throw new RuntimeException("Failed to load password file", e);
                Console.warn("Failed to load password file, creating new");
                Console.warn(e);
            }
            passwords = new JsonObject();
            try {
                Json.write(passwords, new XOREncodedOutputStream(new FileOutputStream(new File(System.getProperty("user.home"), "passwords.json"))));
            } catch(IOException f) {
                if(throwExceptions)
                    throw new RuntimeException("Failed to create password file", f);
                Console.error("Failed to create password file");
                Console.error(f);
            }
        }
        PASSWORDS.clear();
        for(String host : passwords.keySet())
            PASSWORDS.put(host, passwords.getElement(host).as(Login.class));
    }

    public static void main(String[] args) {
        ArgsParser parser = new ArgsParser();
        parser.setName("Java password manager");
        parser.setDescription("Edits and views passwords used by several Java applications.\nVersion 1.1");
        parser.addDefaults();
        parser.addOption('r', "remove", false, "Removes the entry of the specified host");
        parser.addOption('h', "host", true, "Host for the login data to read / write");
        parser.addOption('u', "username", true, "Username to set");
        parser.addOption('p', "password", true, "Password to set");
        parser.addOption('s', "show", null, "Show an entry, or all if 'host' is not specified. May specify with u/p to only show username or password");
        parser.setArgsMode(ArgsParser.ArgsMode.NOT_ALLOWED);
        Options options = parser.parse(args);

        if(options.is("show")) {
            if(options.is("remove")) {
                System.err.println("Multiple tasks specified. Please choose one task only.");
                System.exit(1);
            }
            String host = options.get("host");
            if(host != null) {
                char type = options.get("show").charAt(0);
                if(!PASSWORDS.containsKey(host)) {
                    System.err.println("No entry for host '" + host + "'");
                    System.exit(2);
                }
                Login login = get(host);
                if(type == 'u')
                    System.out.println(login.username);
                else if(type == 'p')
                    System.out.println(login.password);
                else
                    System.out.println(login.toJson());
            }
            else System.out.println(PASSWORDS);
        }
        else if(options.is("remove")) {
            String host = options.get("host");
            if(host == null) {
                System.err.println("Host missing (-o / --host)");
                System.exit(3);
            }
            if(remove(host))
                System.out.println("Entry removed.");
            else
                System.out.println("No entry present anyways.");
        }
        else {
            String host = options.get("host");
            String username = options.get("username");
            String password = options.get("password");
            if(host == null || username == null || password == null) {
                System.err.println("Missing required information");
                if(host == null) System.err.println("Host missing (-o / --host)");
                if(username == null) System.err.println("Username missing (-u / --username)");
                if(password == null) System.err.println("Password missing (-p / --password)");
                System.out.println("Usage: passwords -o/--host <hostname> -u/--username <username> -p/--password <password>");
                System.out.println("Usage: passwords -e/--remove -o/--host <hostname>");
                System.out.println("Usage: passwords -s/--show [u|p] [-o/--host <hostname>]");
                System.out.println("\nUse --help to show all options");
                System.exit(4);
            }
            set(host, username, password);
            System.out.println("Login information set.");
        }
    }
}
