package com.racer.RacerServer;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;


@Slf4j
public class ServerThread extends Thread {
    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);


            
            
            
            
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