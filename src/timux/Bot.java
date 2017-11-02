package timux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;


public class Bot {

	public static JDA jda;
	
	private static AccesMySQL sql;
	
	private static AudioPlayer mainPlayer;

	public static void main(String[] args) throws IOException
	{
		prepareExitHandler();
		
		GetPropertyValues properties = new GetPropertyValues();
		List<String> props = properties.getPropValues();
		
		
		sql = new AccesMySQL(props.get(1), props.get(2), props.get(3));
		mainPlayer = new AudioPlayer();
		
		try {
			jda = new JDABuilder(AccountType.BOT)
					.setToken(props.get(0))
					.buildBlocking();
			jda.setAutoReconnect(true);
			jda.addEventListener(new BotListener(sql, jda, mainPlayer));
		} catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
			e.printStackTrace();
		}
		
		String line;
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
		{
			while(null != (line = reader.readLine()))
			{
				System.out.println(line);
				System.out.print(">");
			}
		} catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	private static void prepareExitHandler () 
	{
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run () {

				jda.shutdown();
				System.out.println("Socrate - Au revoir...");

			}
		}));
	}
}
