package com.racer.RacerServer;


import com.racer.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RacerServerApplication {

	public static void main(String[] args) {

		try (ServerSocket serverSocket = new ServerSocket(Constants.PORT)) {

			System.out.println("Server is listening on port " + Constants.PORT);

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("New client connected");

				new ServerThread(socket).start();
			}

		} catch (IOException ex) {
			System.out.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

}
