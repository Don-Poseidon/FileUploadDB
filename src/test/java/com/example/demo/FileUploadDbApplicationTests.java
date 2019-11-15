package com.example.demo;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileUploadDbApplicationTests {
  @Test
  public void Try_initLog() throws IOException {
    Assert.assertEquals(true, Logger.init_log());
  }

  @Test
  public void Try_ReadConfig() throws IOException {
    Assert.assertEquals(true, ConfEngine.Read_Config());
  }

  @Test
  public void Try_InitDB() throws IOException {
    Logger.init_log();
    ConfEngine.Read_Config();
    Assert.assertEquals(true, DBEngine.InitDB());
  }

  @Test
  public void Try_Insert() throws IOException {
    Logger.init_log();
    ConfEngine.Read_Config();
    DBEngine.InitDB();
    DBEngine.OpenConn();
    DBEngine.DisableAutoCommit();
    DBEngine.Commit();
    int rand = ThreadLocalRandom.current().nextInt(1000 * 1000, 1000 * 1000 * 1000);
    DBEngine.Insert_Update_DB(new DBRow(rand, "ABC", 555.555));
    Assert.assertNotNull(DBEngine.Get_DBRow(rand));
    DBEngine.Rollback();
  }

  @Test
  public void Try_Update() throws IOException {
    Logger.init_log();
    ConfEngine.Read_Config();
    DBEngine.InitDB();
    DBEngine.OpenConn();
    DBEngine.DisableAutoCommit();
    DBEngine.Commit();
    int rand = ThreadLocalRandom.current().nextInt(1000 * 1000, 1000 * 1000 * 1000);
    DBEngine.Insert_Update_DB(new DBRow(rand, "ABC", 555.555));
    DBEngine.Insert_Update_DB(new DBRow(rand, "CCC", 555.555));
    Assert.assertEquals(true, DBEngine.Get_DBRow(rand).Name == "CCC");
    DBEngine.Rollback();
  }
}
