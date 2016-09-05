package com.picarious.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

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

    public String read(String key) {
        File file =  fileHelper.openFile(cacheRoot, key, suffix);
        if (file == null) {
            return null;
        }
        return readLine(file);
    }

    public void write(String key, String line) {
        File file = fileHelper.createFile(cacheRoot, key, suffix);
        writeLine(file, line);
    }

    String readLine(File file) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String line = scanner.nextLine();
        scanner.close();
        return line;
    }

    void writeLine(File file, String line) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(line);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
