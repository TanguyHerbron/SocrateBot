package timux;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Random;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.Statement;

/**
 * @author Tiji
 * 
 * Class for database management
 */

public class AccesMySQL {

	private Connection cn = null;
	private String address;
	private String login;
	private String password;

	/**
	 * Setting up address, login and password of the database
	 * @param address
	 * @param login
	 * @param password
	 */
	public AccesMySQL(String address, String login, String password)
	{
		this.login = login;
		this.address = address;
		this.password = password;
	}

	/**
	 * Return a random quote from the database
	 * @return
	 */
	public String getRandomQuote()
	{
		Statement st = null;
		String str = null;
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			cn = (Connection) DriverManager.getConnection(address, login, password);
			
			st = (Statement) cn.createStatement();			
			String sql = "SELECT citation FROM Socrate.Citations ORDER BY RAND() LIMIT 1";

			ResultSet result = (ResultSet) st.executeQuery(sql);

			if(result.first())
			{
				str = result.getString("citation");				
			}

		} catch(SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {

			if(cn != null)
			{
				try {
					cn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(st != null)
			{
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return str;
	}
	
	public String getAssociatedChannel(String voiceChannelId)
	{
		Statement st = null;
		String str = null;
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			cn = (Connection) DriverManager.getConnection(address, login, password);
			
			st = (Statement) cn.createStatement();
			String sql = "SELECT text FROM Socrate.channelLinks WHERE voice = " + voiceChannelId;
			
			ResultSet result = (ResultSet) st.executeQuery(sql);
			
			if(result.first())
			{
				str = result.getString("text");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			
			if(cn != null)
			{
				try {
					cn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(st != null)
			{
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return str;
	}
	
	public void linkChannels(String textChannel, String voiceChannel)
	{
		Statement st = null;

		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			cn = (Connection) DriverManager.getConnection(address, login, password);
			
			st = (Statement) cn.createStatement();
			String sql = "INSERT INTO Socrate.channelLinks (text, voice) VALUES ('" + textChannel + "', '" + voiceChannel + "')";
			
			st.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			
			if(cn != null)
			{
				try {
					cn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(st != null)
			{
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean checkBlacklist(String url)
	{
		Statement st = null;
		
		boolean val = false;
		
		url = url.substring(url.indexOf("watch?v=")+8);
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			cn = (Connection) DriverManager.getConnection(address, login, password);
			
			st = (Statement) cn.createStatement();
			String sql = "SELECT url FROM Socrate.blacklist WHERE url='" + url + "'";
			
			ResultSet result = (ResultSet) st.executeQuery(sql);
			
			if(result.first())
			{
				val = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			
			if(cn != null)
			{
				try {
					cn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(st != null)
			{
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return val;
	}
	
	public void addBlacklist(String url)
	{
		Statement st = null;
		
		url = url.substring(url.indexOf("watch?v=")+8);
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			cn = (Connection) DriverManager.getConnection(address, login, password);
			
			st = (Statement) cn.createStatement();
			String sql = "INSERT INTO Socrate.blacklist (url) VALUES ('" + url + "')";
			
			st.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			
			if(cn != null)
			{
				try {
					cn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(st != null)
			{
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}		
		}		
	}
	
	public void removeBlacklist(String url)
	{
		Statement st = null;
		
		url = url.substring(url.indexOf("watch?v=")+8);
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			cn = (Connection) DriverManager.getConnection(address, login, password);
			
			st = (Statement) cn.createStatement();
			String sql = "DELETE FROM Socrate.blacklist WHERE url='" + url + "'";
			
			st.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			
			if(cn != null)
			{
				try {
					cn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(st != null)
			{
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void setInfoChannel(String guildId, String channelId)
	{
		Statement st = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = (Connection) DriverManager.getConnection(address, login, password);
			
			st = (Statement) cn.createStatement();
			String sql = "DELETE FROM Socrate.infoChannels WHERE guildId='" + guildId + "'";
			
			st.executeUpdate(sql);
			
			sql = "INSERT INTO Socrate.infoChannels (guildId, channelId) VALUES('" + guildId + "', '" + channelId + "')";
			
			st.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			
			if(cn != null)
			{
				try {
					cn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(st != null)
			{
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public String getInfoChannel(String guildId)
	{
		Statement st = null;
		String str = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = (Connection) DriverManager.getConnection(address, login, password);
			
			st = (Statement) cn.createStatement();
			String sql = "SELECT channelId FROM Socrate.infoChannels WHERE guildId='" + guildId + "'";
			
			ResultSet result = (ResultSet) st.executeQuery(sql);
			
			if(result.first())
			{
				str = result.getString("channelId");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			
			if(cn != null)
			{
				try {
					cn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(st != null)
			{
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return str;
	}
	
	public String selectRandomCaracters()
	{
		Statement st = null;
		String str = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = (Connection) DriverManager.getConnection(address, login, password);
			
			st = (Statement) cn.createStatement();
			String sql = "SELECT words FROM Socrate.frenchWords ORDER BY RAND() LIMIT 1";
			
			ResultSet result = (ResultSet) st.executeQuery(sql);
			
			if(result.first())
			{
				str = result.getString("words");
				
				Random rn = new Random();
				int rand1 = rn.nextInt(2) + 2;
				int rand2 = rn.nextInt(str.length() - rand1);
				
				str = str.substring(rand2, rand2+rand1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			
			if(cn != null)
			{
				try {
					cn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return str;
	}
}
