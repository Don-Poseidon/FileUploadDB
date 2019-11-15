package com.example.demo;

import com.example.demo.Logger;
import com.example.demo.ConfEngine;

import java.io.File;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.example.demo", "controller" })

/**
 * The entry and exit point of the entire program. Initializes the Configuration
 * reader and structure of the DataBase. Creates UPLOAD_FOLDER and
 * PROCESSED_FOLDER if they do not already exist. The same as Main() method in
 * any other program.
 */
public class FileUploadDbApplication {
  public static void main(String[] args) throws IOException {
    if (!Logger.init_log()) // Inits logger
      return;
    if (!ConfEngine.Read_Config()) // Reads config.ini
      return;
    if (!DBEngine.InitDB()) // Inits DataBase
      return;
    new File(ConfEngine.getUPLOAD_FOLDER()).mkdir();
    new File(ConfEngine.getPROCESSED_FOLDER()).mkdir();

    SpringApplication.run(FileUploadDbApplication.class, args);
  }

}
