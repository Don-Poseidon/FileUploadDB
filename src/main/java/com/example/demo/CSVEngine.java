package com.example.demo;

import com.example.demo.Logger;
import com.example.demo.DBRow;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import com.opencsv.CSVReaderHeaderAware;

/** This class reads the uploaded .csv file and updates the DataBase. */
public final class CSVEngine {
  /** Reads the .csv file and updates the Database accordingly. */
  static public void ReadCSV(String CSVPath) throws IOException {
    try {
      Map<String, String> values;
      CSVReaderHeaderAware it = new CSVReaderHeaderAware(new FileReader(CSVPath));

      while (true) {
        values = it.readMap();
        if (values == null) {
          it.close();
          break;
        }
        DBEngine.Insert_Update_DB(
            new DBRow(Integer.parseInt(values.get("id")), values.get("name"), Double.parseDouble(values.get("value"))));
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    }
  }
}
