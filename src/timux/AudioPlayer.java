package timux;

import java.util.HashMap;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

public class AudioPlayer {

	private final AudioPlayerManager playerManager;
	private final Map<Long, GuildAudioManager> guildAudioManagers;

	public AudioPlayer()
	{
		this.guildAudioManagers = new HashMap<>();
		this.playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	private synchronized GuildAudioManager getGuildAudioPlayer(Guild guild)
	{
		long guildId = Long.parseLong(guild.getId());
		GuildAudioManager guildAudioManager = guildAudioManagers.get(guildId);

		if(guildAudioManager == null)
		{
			guildAudioManager = new GuildAudioManager(playerManager);
			guildAudioManagers.put(guildId, guildAudioManager);
		}

		guild.getAudioManager().setSendingHandler(guildAudioManager.getSendHandler());

		return guildAudioManager;
	}

	public void loadAndPlay(final TextChannel channel, final String trackUrl, Member callingMember)
	{
		GuildAudioManager guildAudioManager = getGuildAudioPlayer(channel.getGuild());

		playerManager.loadItemOrdered(guildAudioManager, trackUrl, new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				channel.sendMessage("Ajout du son : " + track.getInfo().title).queue();

				play(channel.getGuild(), guildAudioManager, track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();

				if(firstTrack == null)
				{
					firstTrack = playlist.getTracks().get(0);
				}

				channel.sendMessage("Ajout du son : " + firstTrack.getInfo().title + " (Premier son de la liste " + playlist.getName() + ")").queue();

				play(channel.getGuild(), guildAudioManager, firstTrack);
			}

			@Override
			public void noMatches() {
				channel.sendMessage("Aucun contenu trouvé pour " + trackUrl).queue();
				if(guildAudioManager.scheduler.isTrackEmpty())
				{
					disconnect(callingMember.getVoiceState().getChannel());
				}
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				channel.sendMessage("Impossible de lancer : " + exception.getMessage()).queue();
				if(guildAudioManager.scheduler.isTrackEmpty())
				{
					disconnect(callingMember.getVoiceState().getChannel());
				}
			}
		});
	}

	private void play(Guild guild, GuildAudioManager guildAudioManager, AudioTrack track)
	{
		guildAudioManager.scheduler.queue(track);
	}

	public void skipTrack(TextChannel channel, Member callingMember)
	{
		GuildAudioManager guildAudioManager = getGuildAudioPlayer(channel.getGuild());
		guildAudioManager.scheduler.nextTrack();

		if(guildAudioManager.player.getPlayingTrack() == null)
		{
			disconnect(callingMember.getVoiceState().getChannel());
			channel.sendMessage("Playlist vide, déconnexion.").queue();	
		}
		else
		{
			channel.sendMessage("Passage au son suivant.").queue();			
		}
	}

	private void disconnect(VoiceChannel channel)
	{
		AudioManager audioManager = channel.getGuild().getAudioManager();
		audioManager.closeAudioConnection();
	}

	public void connectToVoiceChannel(VoiceChannel channel)
	{
		AudioManager audioManager = channel.getGuild().getAudioManager();
		audioManager.openAudioConnection(channel);
	}

	public void stop(TextChannel channel, Member callingMember)
	{
		GuildAudioManager guildAudioManager = getGuildAudioPlayer(channel.getGuild());
		guildAudioManager.scheduler.emptyTrack();
		skipTrack(channel, callingMember);
	}

	public void pause(TextChannel channel)
	{
		GuildAudioManager guildAudioManager = getGuildAudioPlayer(channel.getGuild());
		guildAudioManager.scheduler.pause();
		channel.sendMessage("Son mis sur pause.").queue();
	}

	public void resume(TextChannel channel)
	{
		GuildAudioManager guildAudioManager = getGuildAudioPlayer(channel.getGuild());
		if(guildAudioManager.scheduler.isPaused())
		{
			guildAudioManager.scheduler.resume();
			channel.sendMessage("Son relancé.").queue();
		}
		else
		{
			if(guildAudioManager.scheduler.isTrackEmpty() || guildAudioManager.scheduler == null)
			{
				channel.sendMessage("Aucun son à relancer.").queue();
			}
			else
			{
				channel.sendMessage("Son déjà en cours de lecture.").queue();				
			}
		}
	}

	public void volume(TextChannel channel, String[] commandsParams)
	{
		try {
			if(commandsParams.length > 2)
			{
				int vol = Integer.parseInt(commandsParams[2]);
				if(vol <= 100 && vol >= 0)
				{
					GuildAudioManager guildAudioManager = getGuildAudioPlayer(channel.getGuild());
					guildAudioManager.scheduler.setVolume(vol);
					channel.sendMessage("Volume réglé à " + commandsParams[2] +"%.").queue();
				}
				else
				{
					channel.sendMessage("Valeur entrée incorrecte.").queue();
				}
				
			}
			else
			{
				GuildAudioManager guildAudioManager = getGuildAudioPlayer(channel.getGuild());
				channel.sendMessage("Le volume est à " + guildAudioManager.scheduler.getVolume() + "%.").queue();
			}
		} catch(NumberFormatException e)
		{
			channel.sendMessage("Valeur entrée incorrecte.").queue();
		}
	}
	
	public void getSongInfo(TextChannel channel)
	{
		GuildAudioManager guildAudioManager = getGuildAudioPlayer(channel.getGuild());
		
		if(guildAudioManager.player.getPlayingTrack() == null)
		{
			channel.sendMessage("Aucun son en cours...").queue();
		}
		else
		{
			long hoursPassed, minutesPassed, secondsPassed;
			long totalHours, totalMinutes, totalSeconds;
			
			hoursPassed = (long) Math.floor(guildAudioManager.player.getPlayingTrack().getPosition() / 3600.0 / 1000);
			minutesPassed = (long) Math.floor((guildAudioManager.player.getPlayingTrack().getPosition() / 3600.0 / 1000 - hoursPassed) * 60);
			secondsPassed = (long) (((guildAudioManager.player.getPlayingTrack().getPosition() / 3600.0 / 1000 - hoursPassed) * 60 - minutesPassed) * 60);
			
			totalHours = (long) Math.floor(guildAudioManager.player.getPlayingTrack().getDuration() / 3600.0 / 1000);
		    totalMinutes = (long) Math.floor((guildAudioManager.player.getPlayingTrack().getDuration() / 3600.0 / 1000 - totalHours) * 60);
		    totalSeconds = (long) (((guildAudioManager.player.getPlayingTrack().getDuration() / 3600.0 / 1000 - totalHours) * 60 - totalMinutes) * 60);
		    
		    if(guildAudioManager.player.getPlayingTrack().getDuration() == 9223372036854775807L)
		    {
		    	channel.sendMessage("Live en cours : " + guildAudioManager.player.getPlayingTrack().getInfo().title + " [En cours depuis " + hoursPassed + ":" + String.format("%02d", minutesPassed) + ":" + String.format("%02d", secondsPassed) + "]").queue();
		    }
		    else
		    {
			    if(totalHours != 0)
			    {
			    	channel.sendMessage("Son en cours : " + guildAudioManager.player.getPlayingTrack().getInfo().title + " [" + hoursPassed + ":" + String.format("%02d", minutesPassed) + ":" + String.format("%02d", secondsPassed) + "/" + totalHours + ":" + String.format("%02d", totalMinutes) + ":" + String.format("%02d", totalSeconds) + "]").queue();
			    }
			    else
			    {
			    	channel.sendMessage("Son en cours : " + guildAudioManager.player.getPlayingTrack().getInfo().title + " [" + String.format("%02d", minutesPassed) + ":" + String.format("%02d", secondsPassed) + "/" + String.format("%02d", totalMinutes) + ":" + String.format("%02d", totalSeconds) + "]").queue();
			    }
		    }
			
		}
	}
}
