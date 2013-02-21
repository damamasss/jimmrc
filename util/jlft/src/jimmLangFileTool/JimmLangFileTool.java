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
 File: src/jimmLangFileTool/JimmLangFileTool.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimmLangFileTool;

import javax.swing.JOptionPane;


public class JimmLangFileTool
{

	// Variables
	public static String baseLANGFile = "src/lng/EN.lang";
	public static String compareLANGFile = "src/lng/DE.LANG";
	
	// The both files we wound like to compare
	private LGFile base, compare;
	
	public JimmLangFileTool()
	{
		base = new LGFile(baseLANGFile);
		compare = new LGFile(compareLANGFile);
	}

	
	/**
	 * @return Returns the base.
	 */
	public LGFile getBase()
	{
		return base;
	}

	
	/**
	 * @return Returns the compare.
	 */
	public LGFile getCompare()
	{
		return compare;
	}

	
	/**
	 * @param base The base to set.
	 */
	public void setBase(LGFile base)
	{
		this.base = base;
	}


	/**
	 * @param compare The compare to set.
	 */
	public void setCompare(LGFile compare)
	{
		this.compare = compare;
	}


	public void compare()
	{
		LGString lgs_base,lgs_compare;
		LGFileSubset comp_group;
		for(int i=0;i<base.size();i++)
		{
			// A hole subset is missing in the compare file, add subset from base file
			comp_group = compare.containsGroup(((LGFileSubset)base.get(i)).getId());
			if(comp_group == null)
			{
				System.out.println(((LGFileSubset)base.get(i)).getId());
				LGFileSubset temp = ((LGFileSubset)base.get(i)).getClone();
				for(int j=0;j<temp.size();j++)
					((LGString)temp.get(j)).setTranslated(LGString.NOT_TRANSLATED);
				compare.add(i,temp);
			}
			// Only a few items are missing, find out which, add and tag them
			else
			{
				for(int k=0;k<((LGFileSubset)base.get(i)).size();k++)
				{
					lgs_base = (LGString)((LGFileSubset)base.get(i)).get(k);
					lgs_base.setTranslated(LGString.TRANSLATED);
					lgs_compare = comp_group.containsKey(lgs_base.getKey());
					if(lgs_compare == null)
					{
						lgs_compare = lgs_base.getClone();
						lgs_compare.setTranslated(LGString.NOT_TRANSLATED);
						comp_group.add(k,lgs_compare);
					}
					else
						if(lgs_compare.getTranslated() != LGString.NOT_TRANSLATED)
							lgs_compare.setTranslated(LGString.TRANSLATED);
				}
			}
		}
	}


	static public void main(String[] argv)
	{
		JimmLangFileTool tool = new JimmLangFileTool();
		GUI ui = new GUI(tool);
		try
		{
			tool.setBase(LGFile.load(baseLANGFile));
			tool.setCompare(LGFile.load(compareLANGFile));
		} catch (Exception e)
		{
			JOptionPane.showMessageDialog(ui, "Error loading the file", "Error", JOptionPane.ERROR_MESSAGE);
		}
		tool.compare();
		ui.initialize();
	}

}
