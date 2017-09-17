package timux;

import java.io.IOException;
import java.util.List;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

/* TODO
 * Commenting
 * Add dynamic admins from the database
 * Add Google action/assistant
 * Add help for admins commands
 * (Done) Send help via private message
 * Send feedback for the !link command
 * Create website for more help
 * Create a github fork for the project
 * Create a logo
 * Rename the bot to Daniel
 * -> Add quotes from other philosophes
 * Add chat abilities (look for an API)
 * Add voice synthetiser
 * (Done) Add logs (Sorted by guild by channel)
 * Auto remove commands messages
 * Link channels with channels names instead of channels ids
 * Set info channel with channel name instead of channel id
 * Add !join @name for vocal 
 * Create bombparty game
 * Add config file
 */

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
					.addEventListener(new BotListener(sql, jda, mainPlayer))
					.setToken(props.get(0))
					.buildBlocking();
			jda.setAutoReconnect(true);
		} catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
			e.printStackTrace();
		}
	}

	private static void prepareExitHandler () {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run () {

				jda.shutdown();
				System.out.println("Socrate - Au revoir...");

			}
		}));
	}
}
