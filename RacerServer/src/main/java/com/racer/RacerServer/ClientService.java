package com.racer.RacerServer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {

    DatabaseService databaseService = new DatabaseService();

    public SocketPacket register(SocketPacket input) {

        final String username = input.username;
        final String password = input.password;

        String response = databaseService.addUser(username, password);

        String token = "token"; // TODO will be implemented

        if(response!=null) {
            return SocketPacket.builder()
                    .error(response)
                    .build();
        }

        return SocketPacket.builder()
                .username(username)
                .password(password)
                .token(token)
                .build();
    }

    public SocketPacket login(SocketPacket input) {
        final String username = input.username;
        final String password = input.password;

        String response = databaseService.findUserPassword(username, password);

        String token = "token";

        if(response!=null) {
            return SocketPacket.builder()
                    .error(response)
                    .build();
        }

        return SocketPacket.builder()
                .username(username)
                .password(password)
                .token(token)
                .build();
    }
}
