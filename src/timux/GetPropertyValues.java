package timux;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GetPropertyValues {
	
	List<String> result;
	InputStream inputStream;
	
	public List<String> getPropValues() throws IOException
	{
		
		try 
		{
			Properties prop = new Properties();
			String propFileName = "./config.properties";
			
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			
			if(inputStream != null)
			{
				prop.load(inputStream);
			}
			else
			{
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
					
			result = new ArrayList<String>();
			
			result.add(prop.getProperty("token"));
			result.add(prop.getProperty("sqlAddress"));
			result.add(prop.getProperty("sqlLogin"));
			result.add(prop.getProperty("sqlPwd"));
			
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		
		return result;
		
	}
	
}
