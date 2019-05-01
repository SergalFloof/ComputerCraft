package dan200.computercraft.shared.util;

import java.io.*;

public class IDAssigner
{
	private IDAssigner()
	{
	}
	
	public static int getNextIDFromDirectory( File dir )
	{
		return getNextID( dir, true );
	}
	
	public static int getNextIDFromFile( File file )
	{
		return getNextID( file, false );
	}
	
	private static int getNextID( File location, boolean directory )
	{
		// Determine where to locate ID file
		File lastidFile = null;	
		if( directory )
		{
			location.mkdirs();
			lastidFile = new File( location, "lastid.txt" );
		}
		else
		{
			location.getParentFile().mkdirs();
			lastidFile = location;
		}
		
		// Try to determine the id
		int id = 0;
		if( !lastidFile.exists() )
		{
			// If an ID file doesn't exist, determine it from the file structure
			if( directory && location.exists() && location.isDirectory() )
			{
				String[] contents = location.list();
				for( int i=0; i<contents.length; ++i )
				{
					try {
						int number = Integer.parseInt( contents[i] );
						id = Math.max( number + 1, id );
					} catch( NumberFormatException e ) {
						continue;
					}
				}
			}
		}
		else
		{
			// If an ID file does exist, parse the file to get the ID string
			String idString = "0";
			try
			{
				FileInputStream in = new FileInputStream( lastidFile );
                InputStreamReader isr;
                try
                {
                    isr = new InputStreamReader( in, "UTF-8" );
                }
                catch( UnsupportedEncodingException e )
                {
                    isr = new InputStreamReader( in );
                }
				BufferedReader br = new BufferedReader( isr );
                try
                {
                    idString = br.readLine();
                }
                finally
                {
                    br.close();
                }
			}
			catch( IOException e )
			{
				e.printStackTrace();
				return 0;
			}

			try
			{
				id = Integer.parseInt( idString ) + 1;
			}
			catch( NumberFormatException e )
			{
				e.printStackTrace();
				return 0;
			}
		}
		
		// Write the lastID file out with the new value
		try
		{
			BufferedWriter out = new BufferedWriter( new FileWriter( lastidFile, false ) );
			out.write( Integer.toString( id ) );
            out.newLine();
			out.close();
		}
		catch( IOException e )
		{
			System.out.println( "An error occured while trying to create the computer folder. Please check you have relevant permissions." );
			e.printStackTrace();
		}
		
		return id;
	}
}
