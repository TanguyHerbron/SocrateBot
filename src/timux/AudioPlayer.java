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
	private final Map<Long, GuildAudioManager> audioManagers;

	public AudioPlayer()
	{
		this.audioManagers = new HashMap<>();
		this.playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
	}

	private synchronized GuildAudioManager getGuildAudioPlayer(Guild guild)
	{
		long guildId = Long.parseLong(guild.getId());
		GuildAudioManager audioManager = audioManagers.get(guildId);

		if(audioManager == null)
		{
			audioManager = new GuildAudioManager(playerManager);
			audioManagers.put(guildId, audioManager);
		}

		guild.getAudioManager().setSendingHandler(audioManager.getSendHandler());

		return audioManager;
	}

	public void loadAndPlay(final TextChannel channel, final String trackUrl, Member callingMember)
	{
		GuildAudioManager audioManager = getGuildAudioPlayer(channel.getGuild());

		playerManager.loadItemOrdered(audioManager, trackUrl, new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				channel.sendMessage("Ajout du son : " + track.getInfo().title).queue();

				play(channel.getGuild(), audioManager, track);				
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				AudioTrack firstTrack = playlist.getSelectedTrack();

				if(firstTrack == null)
				{
					firstTrack = playlist.getTracks().get(0);
				}

				channel.sendMessage("Ajout du son : " + firstTrack.getInfo().title + " (Premier son de la liste " + playlist.getName() + ")").queue();

				play(channel.getGuild(), audioManager, firstTrack);
			}

			@Override
			public void noMatches() {
				channel.sendMessage("Aucun contenu trouvé pour " + trackUrl).queue();
				if(audioManager.scheduler.isTrackEmpty())
				{
					disconnect(callingMember.getVoiceState().getChannel());
				}
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				channel.sendMessage("Impossible de lancer : " + exception.getMessage()).queue();
				if(audioManager.scheduler.isTrackEmpty())
				{
					disconnect(callingMember.getVoiceState().getChannel());
				}
			}
		});
	}

	private void play(Guild guild, GuildAudioManager audioManager, AudioTrack track)
	{
		audioManager.scheduler.queue(track);
	}

	public void skipTrack(TextChannel channel, Member callingMember)
	{
		GuildAudioManager audioManager = getGuildAudioPlayer(channel.getGuild());
		audioManager.scheduler.nextTrack();

		if(audioManager.player.getPlayingTrack() == null)
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

	public void connectToVoiceChannel(Member callingMember)
	{
		VoiceChannel channel = callingMember.getVoiceState().getChannel();
		AudioManager audioManager = channel.getGuild().getAudioManager();
		audioManager.openAudioConnection(channel);
	}

	public void stop(TextChannel channel, Member callingMember)
	{
		GuildAudioManager audioManager = getGuildAudioPlayer(channel.getGuild());
		audioManager.scheduler.emptyTrack();
		skipTrack(channel, callingMember);
	}

	public void pause(TextChannel channel)
	{
		GuildAudioManager audioManager = getGuildAudioPlayer(channel.getGuild());
		audioManager.scheduler.pause();
		channel.sendMessage("Son mis sur pause.").queue();
	}

	public void resume(TextChannel channel)
	{
		GuildAudioManager audioManager = getGuildAudioPlayer(channel.getGuild());
		if(audioManager.scheduler.isPaused())
		{
			audioManager.scheduler.resume();
			channel.sendMessage("Son relancé.").queue();
		}
		else
		{
			if(audioManager.scheduler.isTrackEmpty() || audioManager.scheduler == null)
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
			int vol = Integer.parseInt(commandsParams[1]);
			if(vol <= 100 && vol >= 0)
			{
				GuildAudioManager audioManager = getGuildAudioPlayer(channel.getGuild());
				audioManager.scheduler.setVolume(vol);
				channel.sendMessage("Volume réglé à " + commandsParams[1] +"%.").queue();
			}
			else
			{
				channel.sendMessage("Valeur entrée incorrecte.").queue();
			}
		} catch(NumberFormatException e)
		{
			channel.sendMessage("Valeur entrée incorrecte.").queue();
		}
	}
}
