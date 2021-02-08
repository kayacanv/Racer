package com.racer.RacerServer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class DatabaseService {

    private static final MyLock userPassLock = new MyLock();
    private static final MyLock userDistLock = new MyLock();


    Map<DatabaseType, Map<String,String> > data = new HashMap<>();


    @SneakyThrows
    private void writeLockIt(DatabaseType databaseType) {
        switch (databaseType) {
            case USER_DIST:
                userPassLock.lock();
            case USER_PASS:
                userDistLock.lock();
        }
    }
    @SneakyThrows
    private void readLockIt(DatabaseType databaseType) {
        switch (databaseType) {
            case USER_DIST:
                userPassLock.lock();
            case USER_PASS:
                userDistLock.lock();
        }
    }
    private void writeUnlockIt(DatabaseType databaseType) {
        switch (databaseType) {
            case USER_DIST:
                userPassLock.unlock();
            case USER_PASS:
                userDistLock.unlock();
        }
    }
    private void readUnlockIt(DatabaseType databaseType) {
        switch (databaseType) {
            case USER_DIST:
                userPassLock.unlock();
            case USER_PASS:
                userDistLock.unlock();
        }
    }

    @SneakyThrows
    private void saveFile(DatabaseType databaseType) {
        writeLockIt(databaseType);
        Properties properties = new Properties();

        for (Map.Entry<String, String> entry : data.get(databaseType).entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }

        properties.store(new FileOutputStream(databaseType.toString() + ".data"), null);

        writeUnlockIt(databaseType);
    }

    @SneakyThrows
    private void loadFile(DatabaseType databaseType) {

        readLockIt(databaseType);

        data.put(databaseType, new HashMap<String, String>());

        Properties properties = new Properties();
        properties.load(new FileInputStream(databaseType.toString() + ".data"));

        for (String key : properties.stringPropertyNames()) {
            data.get(databaseType).put(key, properties.get(key).toString());
        }
        readUnlockIt(databaseType);
    }

    public String addUser(final String username, final String password) {
        loadFile(DatabaseType.USER_PASS);

        if(StringUtils.isEmpty(username))
            return "ERROR: username should not contain any spaces";
        if(username.contains(" "))
            return "ERROR: username should not contain any spaces";
        if(data.get(DatabaseType.USER_PASS).containsKey(username))
            return "ERROR: username already taken";

        data.get(DatabaseType.USER_PASS).put(username, password);
        saveFile(DatabaseType.USER_PASS);
        return null;
    }

    public String findUserPassword(final String username, final String password) {
        loadFile(DatabaseType.USER_PASS);

        if(StringUtils.isEmpty(username))
            return "ERROR: username should not contain any spaces";
        if(username.contains(" "))
            return "ERROR: username should not contain any spaces";
        if(!data.get(DatabaseType.USER_PASS).containsKey(username))
            return "ERROR: username already taken";

        if(!data.get(DatabaseType.USER_PASS).get(username).equals(password))
            return "ERROR: password is wrong";

        return null;
    }

    public void update(String username, Double distTaken) {
        DatabaseType type = DatabaseType.USER_DIST;
        loadFile(type);
        double current = 0;
        if(data.get(type).get(username) != null)
            current = Double.parseDouble(data.get(type).get(username));
        current += distTaken;
        data.get(type).put(username, Double.toString(current));
        saveFile(type);
    }


    public Map<String, String> mapData(DatabaseType userDist) {
        DatabaseType type = DatabaseType.USER_DIST;
        loadFile(type);
        return data.get(type);
    }
}

