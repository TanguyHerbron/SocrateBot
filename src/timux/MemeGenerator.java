package timux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class MemeGenerator {

	String username;
	String password;

	public MemeGenerator(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String dumbSpongeBob(String memeTemplate, String message)
	{
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("https://api.imgflip.com/caption_image");
		String url = null;
		
		String upperMessage = message.toUpperCase();
		String lowerMessage = message.toLowerCase();
		
		String finalMessage = "";
		
		for(int i = 0; i < message.length(); i++)
		{
			if(i%2 == 0)
			{
				finalMessage = finalMessage + upperMessage.charAt(i);				
			}
			else
			{
				finalMessage = finalMessage + lowerMessage.charAt(i);				
			}
		}

		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		params.add(new BasicNameValuePair("template_id", memeTemplate));
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("boxes[0][text]", finalMessage));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		if(entity != null)
		{
			InputStream instream;
			try {
				instream = entity.getContent();
				url = getStringFromInputStream(instream);
				url = url.substring(url.indexOf("url"));
				url = url.substring(url.indexOf(":"));
				url = url.substring(url.indexOf("\"")+1, url.lastIndexOf(",")-1);
				url = url.replaceAll("\\\\", "");
			} catch (UnsupportedOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
		return url;
	}
	
	public String perhaps(String memeTemplate, String message)
	{
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("https://api.imgflip.com/caption_image");
		String url = null;

		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		params.add(new BasicNameValuePair("template_id", memeTemplate));
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("boxes[0][text]", message));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		if(entity != null)
		{
			InputStream instream;
			try {
				instream = entity.getContent();
				url = getStringFromInputStream(instream);
				url = url.substring(url.indexOf("url"));
				url = url.substring(url.indexOf(":"));
				url = url.substring(url.indexOf("\"")+1, url.lastIndexOf(",")-1);
				url = url.replaceAll("\\\\", "");
			} catch (UnsupportedOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
		return url;
	}

	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}

}
