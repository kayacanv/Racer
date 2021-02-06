package com.racer.RacerServer;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocketPacket {
    String description;
    String user;
    String key;
}
