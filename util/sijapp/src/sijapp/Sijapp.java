/*******************************************************************************
SiJaPP - Simple Java PreProcessor
Copyright (C) 2003  Manuel Linsmayer

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*******************************************************************************/


package sijapp;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Vector;


public class Sijapp {


  // Java source file extension
  public static String JAVASRC_EXT = ".java";
  
  public static String LANG_EXT = ".lang";


  /*************************************************************************** */


  // Source diretory
  private File srcDir;


  // Destination directory
  private File destDir;


  // Files
  private String[] filenames;


  // Constructor
  public Sijapp(File srcDir, File destDir) {
    this.srcDir = new File(srcDir.getPath());
    this.destDir = new File(destDir.getPath());
    this.filenames = this.scanDir(this.srcDir, "");
  }


  // Scans the given directory (srcDir/srcDirExt) for Java source files
  private String[] scanDir(File srcDir, String srcDirExt) {

    // Initalize vector
    Vector filenames = new Vector();

    // Get all Java source file in the current directory
    File[] files = (new File(srcDir, srcDirExt)).listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isFile() && 
          (files[i].getName().endsWith(Sijapp.JAVASRC_EXT) || files[i].getName().endsWith(Sijapp.LANG_EXT)) )
      
      {
        filenames.add(srcDirExt + File.separator + files[i].getName());
      }
      else if (files[i].isDirectory()) {
        filenames.addAll(Arrays.asList(this.scanDir(srcDir, srcDirExt + File.separator + files[i].getName())));
      }
    }

    // Return Vector as array
    String[] ret = new String[filenames.size()];
    filenames.copyInto(ret);
    return (ret);

  }


  // Preprocess files
  public void run(Preprocessor pp) throws SijappException {

    // Loop through all files
    for (int i = 0; i < this.filenames.length; i++) {

      // Open source file
      File srcFile = new File(this.srcDir, this.filenames[i]);
      BufferedReader reader;
      try {
        InputStreamReader isr= new InputStreamReader(new FileInputStream(srcFile),"UTF-8");
        reader = new BufferedReader(isr);
      }
      catch (Exception e) {
        throw (new SijappException("File " + srcFile.getPath() + " could not be read"));
      }

      // Open destination file
      File destFile = new File(this.destDir, this.filenames[i]);
      BufferedWriter writer;
      try {
        (new File(destFile.getParent())).mkdirs();
        OutputStreamWriter osw=new OutputStreamWriter(new FileOutputStream(destFile),"UTF-8");
        writer = new BufferedWriter(osw);
      }
      catch (Exception e) {
        throw (new SijappException("File " + destFile.getPath() + " could not be written"));
      }

      // Preprocess
      try {
        pp.run(reader, writer);
      }
      catch (SijappException e) {
        try { reader.close(); }
        catch (IOException f) { /* Do nothing */ }
        try { writer.close(); }
        catch (IOException f) { /* Do nothing */ }
        try { destFile.delete(); }
        catch (SecurityException f) { /* Do nothing */ }
        throw (new SijappException(srcFile.getPath() + ":" + e.getMessage()));
      }
      catch (Exception e)
      {
        throw (new SijappException(srcFile.getPath() + ":" + e.toString()));
      }

      // Close files
      try { reader.close(); }
      catch (IOException e) { /* Do nothing */ }
      try { writer.close(); }
      catch (IOException e) { /* Do nothing */ }

    }

  }


}
