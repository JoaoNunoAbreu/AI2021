package Util;

import java.io.*;

public class IO {

    private File logsFile = null;

    public IO() {
        logsFile = new File("log.log");
    }

    public void writeToLogs (String s) {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(logsFile,true));
            output.write(s + "\n");
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( output != null ) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}