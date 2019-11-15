package controller;

import com.example.demo.Logger;
import com.example.demo.ConfEngine;
import com.example.demo.DBEngine;
import com.example.demo.DBRow;
import com.example.demo.CSVEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * The class contains all necessary controllers
 * and is responsible for dynamic generation of HTML files,
 * which then are uploaded to the user.
 */
@Controller
public class FileUploadController {
  /** Returns FileName with extension (NOT full filename with path). */
  private String FixFileExists(final String FilePath, final String FileName) {
    File f = new File(FilePath + "/" + FileName);
    String fileNoExt = FilenameUtils.removeExtension(f.getName());
    String fileExt = FilenameUtils.getExtension(f.getName());
    int i = 1;
    while (f.exists()) {
      String buf = String.format("%s (%d).%s", fileNoExt, i++, fileExt);
      f = new File(FilePath + "/" + buf);
    }
    return f.getName();
  }

  /** Returns uploadview.html to the user. */
  @RequestMapping("/")
  public String UploadPage(Model model) {
    return "uploadview";
  }

  /**
   * Generates and returns uploadstatusview.html to the user. Also retrieves
   * specified .csv file from the user in package mode.
   */
  @RequestMapping("/upload")
  public String handleUpload(HttpServletRequest request, Model model) throws IOException {
    ServletFileUpload upload = new ServletFileUpload();
    String FileNameAndPath = "", FileName = "";
    FileItemIterator iterStream;
    try {
      iterStream = upload.getItemIterator(request);
      OutputStream out = null;
      while (iterStream.hasNext()) {
        FileItemStream item = iterStream.next();
        InputStream stream = item.openStream();
        if (item.getName() == "") // No file was selected error
          return "FileNotSelected";
        else {
          FileNameAndPath = ConfEngine.getUPLOAD_FOLDER() + "/" + item.getName();
          FileName = item.getName();
        }
        if (!item.isFormField()) {
          out = new FileOutputStream(FileNameAndPath);
          IOUtils.copy(stream, out);
        }
      }
      out.close();
      Class.forName("org.h2.Driver");
      DBEngine.OpenConn();
    } catch (FileUploadException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    }

    Logger.Push_Message(String.format("Start reading file \"%s\"", FileNameAndPath));
    DBEngine.Clear_Counters(); // Clear counters of Inserted and Updated rows
    CSVEngine.ReadCSV(FileNameAndPath); // Read uploaded CSV and update DataBase
    Logger.Push_Message(
        String.format("Finished reading file \"%s\" %n" + "\tRows inserted = %d%n" + "\tRows updated = %d%n",
            FileNameAndPath, DBEngine.GetInserted(), DBEngine.GetUpdated()));

    try { // Moving file to PROCESSED_FOLDER
      String FileNameNew = FixFileExists(ConfEngine.getPROCESSED_FOLDER(), FileName);
      FileUtils.moveFile(new File(FileNameAndPath), new File(ConfEngine.getPROCESSED_FOLDER() + "/" + FileNameNew));
      Logger.Push_Message(String.format("Moved processed file \"%s\"%n" + "\tto PROCESSED_FOLDER%n\"%s\"",
          FileNameAndPath, ConfEngine.getPROCESSED_FOLDER() + "/" + FileNameNew));
    } catch (IOException e) {
      e.printStackTrace();
      Logger.Push_Message(e.getMessage());
    }
    model.addAttribute("msg", "Successfully uploaded file " + "\"" + FileName + "\"");
    List<DBRow> DBContents = DBEngine.GetDB();
    int Original_Size = DBContents.size();
    if (DBContents.size() == DBEngine.MAX_ROWS + 1)
      DBContents.remove(DBContents.size() - 1);
    model.addAttribute("DBContents", DBContents);
    model.addAttribute("Original_Size", Original_Size);
    DBEngine.CloseConn();
    return "uploadstatusview";
  }

  /**
   * Generates and returns uploadjsonview.html to the user. Generates all
   * necessary JSON output.
   */
  @RequestMapping("/json")
  public String JsonPage(Model model) throws IOException {
    DBEngine.OpenConn();
    model.addAttribute("DBContents", DBEngine.GetDB());
    model.addAttribute("JsonList", DBEngine.GetJson());
    DBEngine.CloseConn();
    return "uploadjsonview";
  }
}
