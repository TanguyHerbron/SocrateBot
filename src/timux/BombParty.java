package timux;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.requests.RestAction;

public class BombParty {
	
	private AccesMySQL sql;
	private Guild guild;
	private List<Member> memberList;
	private String messageId;
	private Timer timer;
	private boolean playing;

	public BombParty(AccesMySQL sql, Guild guild)
	{
		this.sql = sql;
		this.guild = guild;
		this.memberList = new ArrayList<>();
		this.playing = false;
	}
	
	public void start()
	{
		timer = new Timer();
		int counter = 0;
		TextChannel chan = (TextChannel) guild.getController().createTextChannel("bombparty").complete();
		chan.createPermissionOverride(guild.getPublicRole()).setDeny(Permission.MESSAGE_READ.getRawValue()).queue();
		
		
		for(int i = 0; i < memberList.size(); i++)
		{
			chan.createPermissionOverride(memberList.get(i)).setAllow(Permission.MESSAGE_READ.getRawValue()).queue();
		}
		
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("BombParty");
		builder.setDescription("Tic Tac...");
		for(int i = 0; i < memberList.size(); i++)
		{
			if(i==0)
			{
				builder.addField(new Field("**" + memberList.get(i).getEffectiveName() + "**", ":heart: :heart: :broken_heart:", true));
			}
			else
			{
				builder.addField(new Field(memberList.get(i).getEffectiveName(), ":heart: :heart: :broken_heart:", true));				
			}
		}
		builder.addBlankField(false);
		builder.addField(new Field(memberList.get(0).getEffectiveName() + " à toi de jouer.", "", false));
		builder.addBlankField(false);
		builder.addField(new Field("Trouver un mot Français contenant " + sql.selectRandomCaracters().toUpperCase(), "", false));
		
		
		MessageEmbed message = builder.build();
		
		RestAction<Message> messageAction = chan.sendMessage(message);
		messageId = messageAction.complete().getId();

		playing = true;
		timer.scheduleAtFixedRate(new BombPartyTimer(counter, chan, messageId), 1000, 1000);
	}
	
	public void addPlayer(TextChannel chan, Member member)
	{
		memberList.add(member);
		chan.sendMessage("<@" + member.getUser().getIdLong() + "> a bien été ajouté à la liste des participants.").queue();
	}
	
	public void removePlayer(TextChannel chan, Member member)
	{
		memberList.remove(member);
		chan.sendMessage("<@" + member.getUser().getIdLong() + "> a bien été retiré de la liste des participants.").queue();
	}
	
	public boolean isRegistered(Member member)
	{
		boolean val = false;
		
		if(memberList.contains(member))
		{
			val = true;
		}
		
		return val;
	}
	
	public List<Member> getMemberList()
	{
		return memberList;
	}
	
	public void stopTimer()
	{
		if(timer != null)
		{
			timer.cancel();
			timer.purge();
		}
	}
	
	public String getGuildId()
	{
		return guild.getId();
	}
	
	public boolean isPlaying()
	{
		return playing;
	}
	
	public void editMessage(TextChannel chan)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("BombParty");
		builder.setDescription("Tic Tac...");
		for(int i = 0; i < memberList.size(); i++)
		{
			builder.addField(new Field(memberList.get(i).getEffectiveName(), ":heart: :heart: :broken_heart:", true));
		}
		builder.addBlankField(false);
		
		
		
		builder.addField(new Field(memberList.get(0).getEffectiveName() + " à toi de jouer.", "", false));
		builder.addBlankField(false);
		builder.addField(new Field("Trouver un mot Français contenant " + sql.selectRandomCaracters().toUpperCase(), "", false));
		
		MessageEmbed message = builder.build();
				
		Message editMessage = chan.getMessageById(messageId).complete();
		
		editMessage.editMessage(message).queue();
	}
}
