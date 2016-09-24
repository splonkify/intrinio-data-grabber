package com.picarious.cache;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;

@Service
public class FileHelper {
    public void deleteFile(String cacheRoot, String key, String suffix) {
        String filePath = makeFilePath(cacheRoot, key, suffix);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
    public FileReader openFile(String cacheRoot, String key, String suffix) {
        String filePath = makeFilePath(cacheRoot, key, suffix);
        try {
            FileReader fileReader = new FileReader(filePath);
            return fileReader;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public FileWriter createFile(String cacheRoot, String key, String suffix) {
        String filePath = makeFilePath(cacheRoot, key, suffix);
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            return fileWriter;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String makeFilePath(String cacheRoot, String key, String suffix) {
        return String.format("%s/%s.%s", cacheRoot, key, suffix);
    }
}
