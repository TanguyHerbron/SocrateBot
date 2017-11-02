package timux;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter{

	private final AudioPlayer player;
	private final BlockingQueue<AudioTrack> queue;
	
	public TrackScheduler(AudioPlayer player)
	{
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
	}

	
	public void queue(AudioTrack track)
	{
		if(!player.startTrack(track, true))
		{
			queue.offer(track);
		}
	}
	
	public void nextTrack()
	{
		player.startTrack(queue.poll(), false);
	}
	
	public void emptyTrack()
	{
		queue.clear();
	}
	
	public boolean isTrackEmpty()
	{
		return queue.isEmpty();
	}
	
	public void setVolume(int vol)
	{
		player.setVolume(vol);
	}
	
	public int getVolume()
	{
		return player.getVolume(); 
	}
	
	public boolean isPaused()
	{
		return player.isPaused();
	}
	
	public void pause()
	{
		player.setPaused(true);
	}
	
	public void resume()
	{
		player.setPaused(false);
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
	{
		if(endReason.mayStartNext)
		{
			nextTrack();
		}
	}
}
