package com.example.demo;

import com.example.demo.Logger;
import java.io.File;
import java.io.IOException;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

/**
 * This class reads .ini configuration file and adjusts program configuration
 * accordingly.
 */
public final class ConfEngine {
  static private String UPLOAD_FOLDER;
  static private String PROCESSED_FOLDER;
  static private String DB_NAME, DB_PATH, DB_LOGIN, DB_PAS;
  static private String DB_TABLE_NAME;

  static private final String CONF_PATH = "./config.ini";
  static private final String SECTION = "CORE";

  static public String getUPLOAD_FOLDER() {
    return UPLOAD_FOLDER;
  }

  static public String getPROCESSED_FOLDER() {
    return PROCESSED_FOLDER;
  }

  static public String getDB_NAME() {
    return DB_NAME;
  }

  static public String getDB_PATH() {
    return DB_PATH;
  }

  static public String getDB_LOGIN() {
    return DB_LOGIN;
  }

  static public String getDB_PAS() {
    return DB_PAS;
  }

  static public String getDB_TABLE_NAME() {
    return DB_TABLE_NAME;
  }

  /**
   * Reads the configuration from CONF_PATH file. Notice, that SECTION should
   * exist in the file. Returns if configuration file was read successfully.
   */
  static public boolean Read_Config() throws IOException {
    try {
      Wini ini = new Wini(new File(CONF_PATH));
      UPLOAD_FOLDER = ini.get(SECTION, "UPLOAD_FOLDER");
      PROCESSED_FOLDER = ini.get(SECTION, "PROCESSED_FOLDER");
      DB_NAME = ini.get(SECTION, "DB_NAME");
      DB_PATH = ini.get(SECTION, "DB_PATH");
      DB_LOGIN = ini.get(SECTION, "DB_LOGIN");
      DB_PAS = ini.get(SECTION, "DB_PAS");
      DB_TABLE_NAME = ini.get(SECTION, "DB_TABLE_NAME");

    } catch (InvalidFileFormatException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
      return false;
    }
    String msg = String.format(
        "Configuration read:%n" + "\tUPLOAD_FOLDER = %s%n" + "\tPROCESSED_FOLDER = %s%n" + "\tDB_NAME = %s%n"
            + "\tDB_PATH = %s%n" + "\tDB_LOGIN = %s%n" + "\tDB_PAS = %s%n" + "\tDB_TABLE_NAME = %s%n",
        UPLOAD_FOLDER, PROCESSED_FOLDER, DB_NAME, DB_PATH, DB_LOGIN, DB_PAS, DB_TABLE_NAME);
    Logger.Push_Message(msg);
    return true;
  }
}
