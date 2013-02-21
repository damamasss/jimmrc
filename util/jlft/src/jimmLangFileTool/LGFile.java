/*******************************************************************************
 JimmLangFileTool - Simple Java GUI for editing/comparing Jimm language files
 Copyright (C) 2005  Jimm Project

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
 ********************************************************************************
 File: src/jimmLangFileTool/LGFile.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimmLangFileTool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Vector;

public class LGFile extends Vector
{

	
	private static final long serialVersionUID = 1L;
	private String name;

	// Array with error specific comments
	private String error[] = {"Generic errors",
			"Login specific errors",
			"Network communication specific exceptions (first half main connection second half peer)",
			"Parsing specific error",
			"Action errors",
			"Specific action errors",
			"Specific action errors",
			"Other errors",
			"Camera errors",
			"File transfer errors",
			"",
			"",
			"HTTP Connection errors"};
	
	public LGFile(String _name)
	{
		super();
		name = _name;
	}
	
	public LGFileSubset containsGroup(String key)
	{
		LGFileSubset value = null;
		
		for(int i=0;i<super.size();i++)
		{
			if(super.get(i) instanceof LGFileSubset)
				if(((LGFileSubset)super.get(i)).getId().equals(key))
					value = (LGFileSubset)super.get(i);
		}
		return value;
	}
	
	public void printContent()
	{
		LGFileSubset subset;
		LGString lgs;
		for(int i=0;i<super.size();i++)
		{
			subset = (LGFileSubset)super.get(i);
			System.out.println(subset.getId());
			for(int j=0;j<subset.size();j++)
			{
				lgs = (LGString)subset.get(j);
				if(lgs.isTranslated() == LGString.NOT_TRANSLATED || lgs.isTranslated() == LGString.NOT_IN_BASE_FILE)
					System.out.println(lgs.toString());
			}
				
		}
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return Returns the entrysize.
	 */
	public int getEntrysize()
	{
		int entries = super.size();
		for(int i=0;i<super.size();i++)
		{
			entries += ((LGFileSubset)super.get(i)).size();
		}
		return entries;
	}
	
	public void save(String path) throws Exception
	{
		BufferedWriter file = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), Charset.forName("UTF-8")));
		file.write("// Labels\n");
		LGFileSubset subset;
		LGString lgs;
		boolean print_end = false;
		for (int i = 0; i < this.size(); i++)
		{
			subset = (LGFileSubset) this.get(i);
			if (!subset.isRemoved())
			{
				if (subset.getId().startsWith("TAR_") && !subset.getId().endsWith("_ELSE"))
				{
					file.write("// " + subset.getId().substring(4, subset.getId().length()) + " target special strings\n");
					file.write("//#sijapp cond.if target is \"" + subset.getId().substring(4, subset.getId().length()) + "\"#\n");
					try
					{
						if (((LGFileSubset) this.get(i + 1)).getId().endsWith("_ELSE"))
							print_end = false;
						else
							print_end = true;
					} catch (Exception e)
					{
						print_end = true;
					}
				}
				else
					if (subset.getId().startsWith("MOD_") && !subset.getId().endsWith("_ELSE"))
					{
						file.write("// " + subset.getId().substring(4, subset.getId().length()) + " module strings\n");
						file.write("//#sijapp cond.if modules_" + subset.getId().substring(4, subset.getId().length()) + " is \"true\" #\n");
						print_end = true;
					}
					else
						if (subset.getId().endsWith("_ELSE"))
						{
							file.write("//#sijapp cond.else#\n");
							print_end = true;
						}
						else
							file.write("// General strings\n");
				for (int j = 0; j < subset.size(); j++)
				{
					lgs = (LGString) subset.get(j);
					if (lgs.getTranslated() != LGString.REMOVED && lgs.getTranslated() != LGString.NOT_TRANSLATED)
					{
						if (lgs.getKey().startsWith("error_"))
						{
							if (lgs.getKey().endsWith("0")) file.write("\n // " + error[Integer.parseInt(lgs.getKey().substring(6, 8)) - 10] + "\n");
						}
						file.write("\"" + lgs.getKey() + "\"\t");
						for (int k = lgs.getKey().length(); k < 22; k += 4)
							file.write("\t");
						file.write("\"" + lgs.getValue() + "\"\n");
					}
				}
				if (print_end)
				{
					print_end = false;
					file.write("//#sijapp cond.end#\n\n");
				}
			}
		}
		file.close();
	}
	
	static public LGFile load(String filename) throws Exception
	{
		String line;
		String group = null;
		LGFileSubset subset = new LGFileSubset();
		LGFileSubset general = new LGFileSubset("GENERAL");
		String name;
		
		if(filename.lastIndexOf("\\") != -1)
			name = filename.substring(filename.lastIndexOf("\\")+1,filename.length());
		else if(filename.lastIndexOf("/") != -1)
			name = filename.substring(filename.lastIndexOf("/")+1,filename.length());
		else
			name = filename;
			
		LGFile temp = new LGFile(name);
		
			BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(filename),Charset.forName("UTF-8")));
			while (file.ready())
			{
				line = file.readLine();
				if (line.lastIndexOf("sijapp") != -1)
				{
					if (line.lastIndexOf("modules") != -1)
						group = "MOD_" + line.substring(line.lastIndexOf("modules") + 8, line.lastIndexOf("is") - 1);
					else
						if (line.lastIndexOf("\"") != -1) 
							group = "TAR_" + line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));

					if (line.lastIndexOf("cond.else") != -1)
					{
						subset.setId(group);
						temp.add(subset.getClone());
						subset = new LGFileSubset();
						group = group + "_ELSE";
					}
					else
						if (line.lastIndexOf("cond.end") != -1)
						{
							subset.setId(group);
							temp.add(subset.getClone());
							subset = new LGFileSubset();
							group = null;
						}
				}
				else
				{
					if (LGString.parseLine(line) != null)
					{
						if (group == null)
							general.add(LGString.parseLine(line));
						else
							subset.add(LGString.parseLine(line));
					}
				}

			}
			temp.add(general);
			return temp;
	}
}
