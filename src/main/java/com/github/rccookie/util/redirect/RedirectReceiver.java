package com.github.rccookie.util.redirect;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import com.github.rccookie.util.Args;
import com.github.rccookie.util.ArgsParser;
import com.github.rccookie.util.Console;

public class RedirectReceiver {

    private static ServerSocket ioServer, errServer;
    private static Socket ioClient, errClient;

    public static void main(String[] rawArgs) throws Exception {
        ArgsParser parser = new ArgsParser();
        parser.setName("I/O Redirection receiver");
        parser.setDescription("Receives output and transfers input from/to a remove program");
        parser.addDefaults();
        parser.addOption('o', "host", true, "Host address for the connection.");
        parser.addOption('p', "port", true, "Port for the connection. Note that the next port will also be used");
        parser.addOption('l', "loop", false, "If enabled the program will wait for the next connection after disconnection");
        parser.allowArgs(false);
        Args args = parser.parse(rawArgs);

        int port = args.getIntOr("port", Redirector.DEFAULT_PORT);
        boolean loop = args.is("loop");

        Console.mapDebug("Address", Inet4Address.getLocalHost().getHostAddress());
        Console.mapDebug("Port", port);
        Console.mapDebug("Error Port", port+1);
        Console.mapDebug("Loop", loop);

        //noinspection LoopConditionNotUpdatedInsideLoop
        do {
            Console.debug("Waiting for connection...");
            try {
                ioServer = new ServerSocket(port);
                errServer = new ServerSocket(port+1);
                ioClient = ioServer.accept();
                errClient = errServer.accept();
                Console.info("Connected to I/O");

                Thread t = new Thread(() -> {
                    try {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = System.in.read(buffer, 0, 8192)) >= 0)
                            ioClient.getOutputStream().write(buffer, 0, read);
                    }
                    catch(IOException e) { Console.debug(e); }
                });
                t.setDaemon(true);
                t.start();
                t = new Thread(() -> {
                    try { errClient.getInputStream().transferTo(System.err); }
                    catch (IOException e) { Console.debug(e); }
                });
                t.setDaemon(true);
                t.start();
                ioClient.getInputStream().transferTo(System.out);
            } catch(SocketException e) {
                if(!"Connection reset".equals(e.getMessage()))
                    throw e;
                Console.info("Disconnected from I/O");
            } finally {
                if(ioServer != null)  ioServer. close();
                if(errServer != null) errServer.close();
                if(ioClient != null)  ioClient. close();
                if(errClient != null) errClient.close();
            }
        } while(loop);
    }
}
