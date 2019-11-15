package com.example.demo;

import com.example.demo.Logger;
import com.example.demo.ConfEngine;
import com.example.demo.DBRow;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for all communications between the DataBase and the
 * program. Also it prepares information for output both in table and in JSON
 * formats.
 */
public final class DBEngine {
  /** Maximum rows from the DataBase, permitted for output. */
  public static final int MAX_ROWS = 1000;
  private static Connection Conn;
  private static boolean Once_Connected = false;
  private static int Rows_Inserted, Rows_Updated;
  private static List<String> JsonList = new ArrayList<String>();

  /** Disable Autocommit feature of Conn. */
  public static void DisableAutoCommit() throws IOException {
    try {
      Conn.setAutoCommit(false);
    } catch (SQLException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    }
  }

  /** Opens Connection to the DataBase */
  public static void OpenConn() throws IOException {
    try {
      Conn = DriverManager.getConnection("jdbc:h2:" + ConfEngine.getDB_PATH() + "/" + ConfEngine.getDB_NAME(),
          ConfEngine.getDB_LOGIN(), ConfEngine.getDB_PAS());
      Once_Connected = true;
    } catch (SQLException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    }
  }

  /** Closes Connection to the DataBase */
  public static void CloseConn() throws IOException {
    try {
      Conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    }
  }

  /** Commits changes made to the DataBase. */
  public static void Commit() throws IOException {
    try {
      if (!Once_Connected || Conn.isClosed()) {
        Conn = DriverManager.getConnection("jdbc:h2:" + ConfEngine.getDB_PATH() + "/" + ConfEngine.getDB_NAME(),
            ConfEngine.getDB_LOGIN(), ConfEngine.getDB_PAS());
        Once_Connected = true;
      }
      Conn.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    }
  }

  /** Undo all changes made to the DataBase until previous commit. */
  public static void Rollback() throws IOException {
    try {
      Conn.rollback();
    } catch (SQLException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    }
  }

  public static void Clear_Counters() {
    Rows_Inserted = Rows_Updated = 0;
  }

  public static int GetInserted() {
    return Rows_Inserted;
  }

  public static int GetUpdated() {
    return Rows_Updated;
  }

  public static List<String> GetJson() {
    return JsonList;
  }

  /**
   * Initializes structure of the DataBase. Still, this method does not fill any
   * data to the DataBase. Returns if the DataBase was initialized successfully.
   */
  public static boolean InitDB() throws IOException {
    boolean alreadyinited = false;
    try {
      if (!Once_Connected || Conn.isClosed()) {
        alreadyinited = false;
        OpenConn();
      } else {
        alreadyinited = true;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    }
    try {
      Class.forName("org.h2.Driver");
      Connection conn = DriverManager.getConnection(
          "jdbc:h2:" + ConfEngine.getDB_PATH() + "/" + ConfEngine.getDB_NAME(), ConfEngine.getDB_LOGIN(),
          ConfEngine.getDB_PAS());
      Statement st = conn.createStatement();
      st.execute("CREATE TABLE IF NOT EXISTS " + ConfEngine.getDB_TABLE_NAME() + "(" + "ID int NOT NULL,"
          + " NAME varchar(255) NOT NULL," + " VALUE double," + " PRIMARY KEY (ID)" + ")");
      if (!alreadyinited) {
        CloseConn();
      }
    } catch (SQLException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
      return false;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
      return false;
    }
    Logger.Push_Message("DataBase initialized");
    return true;
  }

  /**
   * Returns specific dbrow record from the DataBase or null if nothing found.
   * 
   * @throws IOException
   */
  public static DBRow Get_DBRow(int id) {
    try {
      Statement st = Conn.createStatement();
      ResultSet rs = st
          .executeQuery("SELECT * FROM " + ConfEngine.getDB_TABLE_NAME() + " WHERE ID = " + Integer.toString(id));
      if (rs.next()) {
        return new DBRow(Integer.parseInt(rs.getString("ID")), rs.getString("NAME"),
            Double.parseDouble(rs.getString("VALUE")));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Inserts/Updates the DataBase with particular DBRow. Affects Rows_Inserted and
   * Rows_Updated counters.
   */
  public static void Insert_Update_DB(DBRow dbrow) throws IOException {
    try {
      Statement st = Conn.createStatement();
      ResultSet rs = st
          .executeQuery("SELECT * FROM " + ConfEngine.getDB_TABLE_NAME() + " WHERE ID = " + Integer.toString(dbrow.ID));
      if (rs.next()) { // ID was found in the table
        String sql = "Update " + ConfEngine.getDB_TABLE_NAME() + " SET NAME = ?, VALUE = ? WHERE ID = ?";
        PreparedStatement statement = Conn.prepareStatement(sql);
        statement.setString(1, dbrow.Name);
        statement.setString(2, Double.toString(dbrow.Value));
        statement.setString(3, Integer.toString(dbrow.ID));
        statement.executeUpdate();
        Rows_Updated++;
        Logger.Push_Message(
            String.format("DataBase updated: ID = %d, Name = %s, Value = %f", dbrow.ID, dbrow.Name, dbrow.Value));
      } else { // ID was NOT found in the table
        String sql = "INSERT INTO " + ConfEngine.getDB_TABLE_NAME() + " (ID, NAME, VALUE) VALUES (?, ?, ?)";
        PreparedStatement statement = Conn.prepareStatement(sql);
        statement.setString(1, Integer.toString(dbrow.ID));
        statement.setString(2, dbrow.Name);
        statement.setString(3, Double.toString(dbrow.Value));
        statement.executeUpdate();
        Rows_Inserted++;
        Logger.Push_Message(String.format("DataBase new record inserted: ID = %d, Name = %s, Value = %f", dbrow.ID,
            dbrow.Name, dbrow.Value));
      }
    } catch (SQLException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    }
  }

  /**
   * Returns all records, stored in the DataBase as List of DBRow instances. Only
   * first MAX_ROWS records are returned Also Initializes JsonList and fills it
   * with data.
   */
  public static List<DBRow> GetDB() throws IOException {
    List<DBRow> out = new ArrayList<DBRow>();
    JsonList.clear();
    JsonList.add("[");
    try {
      Statement st = Conn.createStatement();
      ResultSet rs = st.executeQuery("SELECT * FROM " + ConfEngine.getDB_TABLE_NAME());
      int count = 1;
      while (rs.next() && count++ <= MAX_ROWS + 1) {
        DBRow dbrow = new DBRow(Integer.parseInt(rs.getString("ID")), rs.getString("NAME"),
            Double.parseDouble(rs.getString("VALUE")));
        out.add(dbrow);
        JsonList.add("  {");
        JsonList.add(String.format("    \"id\": %d,", dbrow.ID));
        JsonList.add(String.format("    \"name\": \"%s\",", dbrow.Name));
        JsonList.add(String.format("    \"value\": %s", String.valueOf(dbrow.Value)));
        JsonList.add("  },");
      }
    } catch (SQLException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    }
    JsonList.set(JsonList.size() - 1, "  }");
    JsonList.add("]");
    return out;
  }
}
