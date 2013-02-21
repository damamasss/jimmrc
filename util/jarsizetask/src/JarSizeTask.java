/*******************************************************************************
JarSizeTask - Ant task to update the MIDlet-Jar-Size property inside a JAR file
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


public class JarSizeTask extends Task
{


	// JAR file to update
    private File file;


    // Sets the file to update
    public void setFile(File file)
    {
		this.file = file;
    }


    // Task implementation
    public void execute() throws BuildException
    {

		// Check whether file exists
    	if (!this.file.isFile())
    	{
    		throw (new BuildException(file.toString() + " does not exist or is not a file"));
    	}

		// Loop counter
		int i = 0;

		// False if Manifest should not be compressed
		boolean compressManifest = true;

		long lengthBefore;
		long lengthAfter;

		// Loop until MIDlet-Jar-Size property and real JAR filesize match
		do
		{

			// Update MIDlet-Jar-Size property
			lengthBefore = this.file.length();
			this.update(compressManifest);
			lengthAfter = this.file.length();

			// Increase loop counter and check loop number
			if (i++ > 2)
			{
				compressManifest = false;
			}

		}
		while (lengthBefore != lengthAfter);

		// Output success message
		System.out.println("MIDlet-Jar-Size property successfully updated");

    }


	// Updates MIDlet-Jar-Size property inside the Manifest
	public void update(boolean compressManifest) throws BuildException
	{

		// Catch some exceptions
		try {

			// Open file to update
			FileInputStream fis = new FileInputStream(this.file);
			ZipInputStream zis = new ZipInputStream(fis);

			// Open temporary file
			File temp = File.createTempFile("jst", ".jar", this.file.getParentFile());
			FileOutputStream fos = new FileOutputStream(temp);
			ZipOutputStream zos = new ZipOutputStream(fos);

			// Best compression
			zos.setLevel(Deflater.BEST_COMPRESSION);

			// Local variables
			ZipEntry ze, newZe;
			CRC32 checksum = new CRC32();
			byte[] buf = new byte[1024];

			// Loop until no more entry is available
			while ((ze = zis.getNextEntry()) != null)
			{

				// Check for Manifest entry
				if (ze.getName().equals("META-INF/MANIFEST.MF"))
				{

					// Read Manifest and update MIDlet-Jar-Size property
					BufferedReader br = new BufferedReader(new InputStreamReader(zis));
					StringBuffer sb = new StringBuffer();
					String line;
					while ((line = br.readLine()) != null)
					{
						if (line.indexOf("MIDlet-Jar-Size") == -1)
						{
							sb.append(line);
						}
						else
						{
							sb.append("MIDlet-Jar-Size: " + this.file.length());
						}
						sb.append('\n');
					}
					zis.closeEntry();

					// Convert to raw bytes
					byte[] raw = sb.toString().getBytes();

					// Create new ZipEntry, update checksum and other attributes
					newZe = new ZipEntry(ze);
					checksum.update(raw);
					ze.setCrc(checksum.getValue());
					ze.setSize(raw.length);
					ze.setMethod(compressManifest ? Deflater.DEFLATED : Deflater.NO_COMPRESSION);
					ze.setCompressedSize(-1);  // Don't know compressed size

					// Write entry
					zos.putNextEntry(ze);
					zos.write(raw, 0, raw.length);
					zos.closeEntry();

				}

				// Other entry
				else
				{

					// Copy entry from original file to temporary file without changing anything
				   newZe = new ZipEntry(ze);
				   newZe.setCompressedSize(-1);  // Don't know compressed size
				   zos.putNextEntry(newZe); 
					while (zis.available() != 0)
					{
						int len = zis.read(buf, 0, 1024);
						if (len != -1)
						{
							zos.write(buf, 0, len);
						}
					}
					zos.closeEntry();
					zis.closeEntry();

				}

			}

			// Close files
			zis.close();
			zos.close();

			// Delete original file, replace with temporary file
			if (!this.file.delete() || !temp.renameTo(this.file))
			{
				throw (new BuildException("Could not delete or create " + file));
			}

		}

		// Rewrite IOException to BuildException
		catch (IOException e)
		{
			throw (new BuildException(e.getMessage()));
		}

	}


}
