package com.picarious.cache;

import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class FileHelper {
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
