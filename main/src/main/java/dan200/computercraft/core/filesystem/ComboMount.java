/**
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2016. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.filesystem;

import dan200.computercraft.api.filesystem.IMount;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ComboMount implements IMount
{	
	private IMount[] m_parts;
	
	public ComboMount( IMount[] parts )
	{
		m_parts = parts;
	}
	
	// IMount implementation
	
	@Override
	public boolean exists( String path ) throws IOException
	{
		for( int i=m_parts.length-1; i>=0; --i )
		{
			IMount part = m_parts[i];
			if( part.exists( path ) )
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isDirectory( String path ) throws IOException
	{
		for( int i=m_parts.length-1; i>=0; --i )
		{
			IMount part = m_parts[i];
			if( part.isDirectory( path ) )
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void list( String path, List<String> contents ) throws IOException
	{
		// Combine the lists from all the mounts
		List<String> foundFiles = null;
		int foundDirs = 0;
		for( int i=m_parts.length-1; i>=0; --i )
		{
			IMount part = m_parts[i];
			if( part.exists( path ) && part.isDirectory( path ) )
			{
				if( foundFiles == null )
				{
					foundFiles = new ArrayList<String>();
				}
				part.list(  path, foundFiles );
				foundDirs++;
			}
		}
		
		if( foundDirs == 1 )
		{
			// We found one directory, so we know it already doesn't contain duplicates
			contents.addAll( foundFiles );
		}
		else if( foundDirs > 1 )
		{
			// We found multiple directories, so filter for duplicates
			Set<String> seen = new HashSet<String>();
			for( int i=0; i<foundFiles.size(); ++i )
			{
				String file = foundFiles.get(i);
				if( seen.add( file ) )
				{
					contents.add( file );
				}
			}
		}
		else
		{
			throw new IOException( "Not a directory" );
		}
	}
	
	@Override
	public long getSize( String path ) throws IOException
	{
		for( int i=m_parts.length-1; i>=0; --i )
		{
			IMount part = m_parts[i];
			if( part.exists( path ) )
			{
				return part.getSize( path );
			}
		}
		throw new IOException( "No such file" );
	}

	@Override
	public InputStream openForRead( String path ) throws IOException
	{
		for( int i=m_parts.length-1; i>=0; --i )
		{
			IMount part = m_parts[i];
			if( part.exists( path ) && !part.isDirectory( path ) )
			{
				return part.openForRead( path );
			}
		}
		throw new IOException( "No such file" );
	}
}
