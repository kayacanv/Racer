package com.racer.RacerServer;

import com.google.gson.Gson;
import com.racer.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;


@Slf4j
public class ServerThread extends Thread {
    private Socket socket;
    ClientService clientService = new ClientService();
    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            new PrintWriter(this.socket.getOutputStream(), true);

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                SocketPacket packet = (new Gson()).fromJson(inputLine, SocketPacket.class);

                switch (packet.operation) {
                    case Constants.O_REGISTER:
                        clientService.register(packet);
                        break;
                    case Constants.O_LOGIN:
                        clientService.login(packet);
                        break;
                }
            }



            /* do {
                 = new String(, StandardCharsets.UTF_8);;

                String reverseText = new StringBuilder(text).reverse().toString();
                writer.println("Server: " + reverseText);
            } while (!text.equals("bye"));
            */

            socket.close();
            log.info("Socket Clossed");

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}