package timux;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class ArtificialInterlligence {
	
	AIConfiguration configuration;
	AIDataService dataService;
	String guildId;
	
	public ArtificialInterlligence(String guildId)
	{
		this.guildId = guildId;
		configuration = new AIConfiguration("07dbedc360324feab7b8942c21a81aa0");
		
		dataService = new AIDataService(configuration);
	}
	
	public String ask(String question)
	{
		String strResponse = "";
		
		try {
			AIRequest request = new AIRequest(question);
		
			AIResponse response = dataService.request(request);
			
			if(response.getStatus().getCode() == 200)
			{
				strResponse = response.getResult().getFulfillment().getSpeech();
			}
			else
			{
				strResponse = response.getStatus().getErrorDetails();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return strResponse;
	}
	
	public String getGuildId()
	{
		return guildId;
	}

}
