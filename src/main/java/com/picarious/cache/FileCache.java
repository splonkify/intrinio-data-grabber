package com.picarious.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class FileCache {
    private final String suffix;
    private final String cacheRoot;
    private final FileHelper fileHelper;

    @Autowired
    public FileCache(@Value("${fileCache.root}") String cacheRoot, @Value("${fileCache.suffix}") String suffix, FileHelper fileHelper) {
        this.cacheRoot = cacheRoot;
        this.suffix = suffix;
        this.fileHelper = fileHelper;
    }

    public void clear(String key) {
        fileHelper.deleteFile(cacheRoot, key, suffix);
    }

    public String read(String key) {
        FileReader fileReader = fileHelper.openFile(cacheRoot, key, suffix);
        if (fileReader == null) {
            return null;
        }
        return readLine(fileReader);
    }

    public void write(String key, String line) {
        FileWriter fileWriter = fileHelper.createFile(cacheRoot, key, suffix);
        writeLine(fileWriter, line);
    }

    String readLine(FileReader fileReader) {
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        try {
            String line = bufferedReader.readLine();
            bufferedReader.close();
            fileReader.close();
            return line;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void writeLine(FileWriter fileWriter, String line) {
        try {
            fileWriter.write(line);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
