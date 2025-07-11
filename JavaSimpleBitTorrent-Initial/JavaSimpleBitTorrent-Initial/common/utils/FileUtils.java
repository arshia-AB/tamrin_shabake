package common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.Map;

import static common.utils.MD5Hash.HashFile;

public class FileUtils {

    public static Map<String, String> listFilesInFolder(String folderPath) {
        Map<String, String> fileHashMap = new HashMap<>();
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("Invalid folder path: " + folderPath);
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return fileHashMap;
        }

        for (File file : files) {
            if (file.isFile()) {
                try {
                    String hash = HashFile(file.getAbsolutePath());
                    fileHashMap.put(file.getName(), hash);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return fileHashMap;
    }

    public static String getSortedFileList(Map<String, String> files) {
        if (files == null || files.isEmpty()) {
            return "";
        }

        List<String> fileNames = new ArrayList<>(files.keySet());
        Collections.sort(fileNames);

        StringBuilder sb = new StringBuilder();
        for (String fileName : fileNames) {
            sb.append(fileName).append(":").append(files.get(fileName)).append("\n");
        }

        return sb.toString().trim();
    }

}
