package timux;

import java.util.TimerTask;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;

public class BombPartyTimer extends TimerTask {
	
	private int seconds;
	private TextChannel chan;
	private String messageId;
	
	public BombPartyTimer(int seconds, TextChannel chan, String messageId)
	{
		this.seconds = seconds;
		this.chan = chan;
		this.messageId = messageId;
	}
	
	public void run()
	{
		/*EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("BombParty");
		builder.setDescription(String.valueOf(seconds));
		builder.addField(new Field("Player1", "Field", true));
		builder.addField(new Field("Player2", "Field", true));
		builder.addField(new Field("Player3", "Field", true));
		builder.addField(new Field("Player4", "Field", true));
		builder.addField(new Field("Player5", "Field", true));
		builder.addField(new Field("Player6", "Field", true));
		builder.addField(new Field("Player7", "Field", true));
		MessageEmbed editedMessage = builder.build();
		
	    chan.editMessageById(messageId, editedMessage).queue();
	    seconds++;*/
	}

}
