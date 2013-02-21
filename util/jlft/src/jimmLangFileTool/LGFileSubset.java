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
 File: src/jimmLangFileTool/LGFileSubset.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher
 *******************************************************************************/

package jimmLangFileTool;

import java.util.Vector;

public class LGFileSubset extends Vector
{

	private static final long serialVersionUID = 1L;
	String id;
	
	boolean removed;
	
	public LGFileSubset(String _id)
	{
		super();
		id = _id;
		removed = false;
	}
	
	public LGFileSubset()
	{
		super();
	}
	
	public LGFileSubset getClone()
	{
		return this;
	}
	
	public LGString containsKey(String key)
	{
		
		for(int i=0;i<super.size();i++)
		{
			if(super.get(i) instanceof LGString)
				if(((LGString)super.get(i)).getKey().equals(key))
					return (LGString) super.get(i);
		}
		return null;
	}

	
	/**
	 * @return Returns the id.
	 */
	public String getId()
	{
		return id;
	}
	
	public String toString()
	{
		return id;
	}

	
	/**
	 * @param id The id to set.
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	
	/**
	 * @return Returns the removed.
	 */
	public boolean isRemoved()
	{
		return removed;
	}

	
	/**
	 * @param removed The removed to set.
	 */
	public void setRemoved(boolean removed)
	{
		this.removed = removed;
	}

}
