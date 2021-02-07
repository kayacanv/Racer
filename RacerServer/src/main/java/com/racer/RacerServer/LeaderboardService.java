package com.racer.RacerServer;

import com.google.gson.Gson;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.Socket;
import java.util.*;

@Service
@NoArgsConstructor
@Slf4j
public class LeaderboardService {
    DatabaseService databaseService = new DatabaseService();


    @SneakyThrows
    public void getLeaderboard(Socket socket) {

        while(socket.isConnected()) {

             Map<String,String> data = databaseService.mapData(DatabaseType.USER_DIST);
            List <UserDistance> list = new ArrayList<>();
            for (Map.Entry<String,String> entry : data.entrySet()) {
                list.add( UserDistance.builder()
                        .username(entry.getKey())
                        .distance(Double.parseDouble(entry.getValue()))
                        .build()
                );
            }
            list.sort(Comparator.comparing(UserDistance::getDistance).reversed());



            socket.getOutputStream().write(new Gson().toJson(
                    SocketPacket.builder()
                    .leaderboard(list)
                    .build()
            ).getBytes());
            socket.getOutputStream().write("\n".getBytes());
            socket.getOutputStream().flush();

            log.info("Sent leaderboard: list: {}", list);
            Thread.sleep(10000);
        }
    }
}
