package com.racer.RacerServer;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocketPacket {
    String operation; // "LOGIN" , "REGISTER"
    String username;
    String password;
    String token;
    String error; // Error
    String success; // Success
    Double distTaken;
    List <UserDistance> leaderboard;
}
