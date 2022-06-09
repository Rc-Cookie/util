package com.github.rccookie.util.redirect;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.github.rccookie.util.Console;

public class Redirector {

    public static final String DEFAULT_HOST = "192.168.2.110";
    public static final int DEFAULT_PORT = 61200;

    public static boolean tryRedirect() {
        return tryRedirect(DEFAULT_HOST, DEFAULT_PORT);
    }

    public static boolean tryRedirect(int port) {
        return tryRedirect(DEFAULT_HOST, port);
    }

    public static boolean tryRedirect(String host) {
        return tryRedirect(host, DEFAULT_PORT);
    }

    public static boolean tryRedirect(String host, int port) {
        try {
            redirect(host, port);
        } catch(UncheckedIOException e) {
            return false;
        }
        return true;
    }

    public static void redirect() throws UncheckedIOException {
        redirect(DEFAULT_HOST, DEFAULT_PORT);
    }

    public static void redirect(int port) throws UncheckedIOException {
        redirect(DEFAULT_HOST, port);
    }

    public static void redirect(String host) throws UncheckedIOException {
        redirect(host, DEFAULT_PORT);
    }

    public static void redirect(String host, int port) throws UncheckedIOException {
        try {
            Console.mapDebug("Redirecting to", port);

            Socket ioSocket  = new Socket(host, port);
            Socket errSocket = new Socket(host, port+1);
            Console.debug("Connected to external I/O");

            System.setIn(ioSocket.getInputStream());
            System.setOut(new PrintStream(ioSocket.getOutputStream()));
            System.setErr(new PrintStream(errSocket.getOutputStream()));
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public static void main(String[] args) throws UnknownHostException {
        Console.getFilter().setEnabled("debug", true);
        redirect("192.168.2.117", DEFAULT_PORT);
        System.out.println("Hello World!");
        System.out.println("Hello World!");
    }
}
