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
 File: src/jimmLangFileTool/LGStrings.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimmLangFileTool;

public class LGString implements Cloneable
{

	private String key, value;
	private int translated;
	private boolean returnKey;
	
	final static public int TRANSLATED   		= 0;
	final static public int NOT_TRANSLATED 		= 1;
	final static public int NEWLY_TRANSLATED	= 2;	
	final static public int NOT_IN_BASE_FILE 	= 3;
	final static public int REMOVED			 	= 4;
	
	public LGString(String _key,String _value,int _trans)
	{
		key = _key;
		value = _value;
		translated = _trans;
		returnKey = false;
	}

	public static LGString parseLine(String line)
	{
		String key,value;
		
		line = line.trim();
		
		if (line.startsWith("//"))
			return null;
		else
		{
			if(line.startsWith("\""))
			{
				String[] keys = line.split("\"\\s*\"");
				key = keys[0].substring(1,keys[0].length());
				value = keys[1].substring(0,keys[1].length()-1);
				return new LGString(key,value,NOT_IN_BASE_FILE);
			}
			return null;
		}
	}

	public LGString getClone()
	{
		try
		{
			return((LGString)this.clone());
		} catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
	/**
	 * @return Returns the key.
	 */
	public String getKey()
	{
		return key;
	}

	
	/**
	 * @param key The key to set.
	 */
	public void setKey(String key)
	{
		this.key = key;
	}

	
	/**
	 * @return Returns the translated.
	 */
	public int isTranslated()
	{
		return translated;
	}

	
	/**
	 * @param translated The translated to set.
	 */
	public void setTranslated(int translated)
	{
		this.translated = translated;
	}

	
	/**
	 * @return Returns the value.
	 */
	public String getValue()
	{
		return value;
	}

	
	/**
	 * @param value The value to set.
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
	
	public void configToString(boolean _key)
	{
		this.returnKey=_key;
	}

	public String toString()
	{
		if(returnKey)
			return(key);
		else
			return(value);
	}

	
	/**
	 * @param returnKey The returnKey to set.
	 */
	public void setReturnKey(boolean returnKey)
	{
		this.returnKey = returnKey;
	}

	
	/**
	 * @return Returns the translated.
	 */
	public int getTranslated()
	{
		return translated;
	}

}
