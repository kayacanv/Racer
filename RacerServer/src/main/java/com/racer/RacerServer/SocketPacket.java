package com.racer.RacerServer;

import lombok.*;

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
}
