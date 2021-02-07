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
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@RequiredArgsConstructor
public class DatabaseService {
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    Map<String, String> userData = new HashMap<String, String>();


    @SneakyThrows
    private void saveFile() {
        lock.writeLock().lock();

        Properties properties = new Properties();

        for (Map.Entry<String, String> entry : userData.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }

        properties.store(new FileOutputStream("User.data"), null);

        lock.writeLock().unlock();
    }

    @SneakyThrows
    private void loadFile() {
        lock.readLock().lock();

        userData.clear();
        Properties properties = new Properties();
        properties.load(new FileInputStream("User.data"));

        for (String key : properties.stringPropertyNames()) {
            userData.put(key, properties.get(key).toString());
        }
        lock.readLock().unlock();
    }

    public String addUser(final String username, final String password) {
        loadFile();

        if(StringUtils.isEmpty(username))
            return "ERROR: username should not contain any spaces";
        if(username.contains(" "))
            return "ERROR: username should not contain any spaces";
        if(userData.containsKey(username))
            return "ERROR: username already taken";

        userData.put(username, password);
        saveFile();
        return null;
    }

    public String findUserPassword(final String username, final String password) {
        loadFile();

        if(StringUtils.isEmpty(username))
            return "ERROR: username should not contain any spaces";
        if(username.contains(" "))
            return "ERROR: username should not contain any spaces";
        if(!userData.containsKey(username))
            return "ERROR: username already taken";

        if(!userData.get(username).equals(password))
            return "ERROR: password is wrong";

        return null;
    }
}

