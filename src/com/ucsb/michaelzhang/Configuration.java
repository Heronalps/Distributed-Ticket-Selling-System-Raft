package com.ucsb.michaelzhang;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class Configuration {

    public static String readConfig(String filename, String key) throws IOException {
        String value = null;
        try(FileReader reader = new FileReader(filename)){
            Properties properties = new Properties();
            properties.load(reader);
            value = properties.getProperty(key);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return value;
    }

    public static void changeProperty (String filename, String key, String value) throws IOException {

        final File tmpFile = new File(filename + ".tmp");
        final File file = new File(filename);

        //New printWriter will empty the file. That's why it needs a tmp.
        PrintWriter pw = new PrintWriter(tmpFile);
        BufferedReader br = new BufferedReader(new FileReader(file));
        boolean found = false;
        final String toAdd = key + '=' + value;

        for (String line; (line = br.readLine()) != null; ) {
            if (line.startsWith(key + '=')) {
                line = toAdd;
                found = true;
            }
            pw.println(line);
        }
        if (!found) {
            pw.println(toAdd);
        }
        br.close();
        pw.close();

        tmpFile.renameTo(file);
    }

    public static void deleteProperty (String filename, String key) throws IOException {

        final File tmpFile = new File(filename + ".tmp");
        final File file = new File(filename);

        //New printWriter will empty the file. That's why it needs a tmp.
        PrintWriter pw = new PrintWriter(tmpFile);
        BufferedReader br = new BufferedReader(new FileReader(file));

        for (String line; (line = br.readLine()) != null; ) {
            if (line.startsWith(key + '=')) {
                line = "";
            }
            pw.println(line);
        }

        br.close();
        pw.close();

        tmpFile.renameTo(file);
    }

    public static void duplicateFile (String source, String destination) throws IOException {
        File sourceFile = new File(source);
        File destFile = new File(destination);
        if (destFile.exists()){
            destFile.delete();
        }
        Files.copy(sourceFile.toPath(), destFile.toPath());
    }

}
