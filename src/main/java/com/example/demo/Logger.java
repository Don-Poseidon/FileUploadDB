package com.example.demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The class is responsible for all the output to the Log file. The Log.log file
 * is created in the project's root directory.
 */
public final class Logger {
  private static BufferedWriter out = null;
  private static FileWriter fstream;
  private static SimpleDateFormat DateFormat = new SimpleDateFormat("'['yyyy.MM.dd HH:mm:ss']'");

  /**
   * Opens Log.log file to append the log data. Returns if the Log file was
   * initialized successfully.
   */
  public static boolean init_log() throws IOException { // Log.log is placed in app's root (./)
    try {
      fstream = new FileWriter("./Log.log", true);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    out = new BufferedWriter(fstream);
    out.newLine();
    Push_Message("-=-=-=-=-=-=-=-Application started-=-=-=-=-=-=-=-");
    return true;
  }

  private static String TimeStamp() {
    return DateFormat.format(new Date());
  }

  /** Appends particular message to the Log file. */
  public static void Push_Message(String msg) throws IOException {
    out.append(TimeStamp() + " " + msg);
    out.newLine();
    out.flush();
  }
}
