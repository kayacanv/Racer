package com.racer.RacerServer;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;


@Slf4j
public class ServerThread extends Thread {
    private Socket socket;
    ClientService clientService = new ClientService();
    LeaderboardService leaderboardService = new LeaderboardService();

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            log.info( "Socket connected: " + socket.getInetAddress());

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            new PrintWriter(this.socket.getOutputStream(), true);

            String inputLine;
            SocketPacket response = null;
            while((inputLine = in.readLine()) != null) {
                SocketPacket packet = (new Gson()).fromJson(inputLine, SocketPacket.class);

                log.info("Packet: {}", packet);

                switch (packet.operation) {
                    case Constants.O_REGISTER:
                        response = clientService.register(packet);
                        break;
                    case Constants.O_LOGIN:
                        response = clientService.login(packet);
                        break;
                    case Constants.O_DISTANCE_UPDATE:
                        response = clientService.distanceTaken(packet);
                        break;
                    case Constants.O_GET_LEADERBOARD:
                        leaderboardService.getLeaderboard(socket);

                }
                log.info("Response: {}", response);
                socket.getOutputStream().write(new Gson().toJson(response).getBytes());
                socket.getOutputStream().write("\n".getBytes());
                socket.getOutputStream().flush();
            }

            socket.close();
            log.info("Socket Clossed");


            /* do {
                 = new String(, StandardCharsets.UTF_8);;

                String reverseText = new StringBuilder(text).reverse().toString();
                writer.println("Server: " + reverseText);
            } while (!text.equals("bye"));
            */


        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}