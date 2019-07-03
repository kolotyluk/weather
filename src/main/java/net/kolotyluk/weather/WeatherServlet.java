package net.kolotyluk.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.function.BinaryOperator;

//import com.oracle.httpclient;

//import java.net.http.HttpClient;
//import java.net.http.HttpClient.Redirect;
//import java.net.http.HttpClient.Version;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.net.http.HttpResponse.BodyHandlers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class WeatherServlet
 */
@WebServlet("/weather")
public class WeatherServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final String WEATHER_APPID = System.getenv().get("WEATHER_APPID");
       
    /**
     * @throws Exception 
     * @see HttpServlet#HttpServlet()
     */
    public WeatherServlet() throws Exception {
    	
        super();
        // TODO Auto-generated constructor stub
        
		if (WEATHER_APPID == null)
		{
			throw new Exception("WEATHER_APPID environment variable is not defined!");
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response
			.getWriter()
			.append("GET: city = ")
			.append(request.getParameter("city")); //.append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		getWeather(request.getParameter("city"));
		// TODO Auto-generated method stub
		// doGet(request, response);
		response
		.getWriter()
		.append("POST: city = ")
		.append(request.getParameter("city"))
		.append("\n\n\n")
		.append(getWeather(request.getParameter("city")));
		//.append(request.getContextPath());

	}
	
	String getWeather(String city) {
		
		// Using simple old fashion Java HTTP Client
		
		String urlString = String.format(
				"http://api.openweathermap.org/data/2.5/weather?q=%s&APPID=%s",
				city,
				WEATHER_APPID);
		
		try {
			URL url = new URL(urlString);
		      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		      connection.setRequestMethod("GET");
		      connection.setDoOutput(true);
		      BufferedReader in = new BufferedReader(
		        new InputStreamReader(connection.getInputStream()));
		      String result = in.lines().reduce("", (a, b) -> a + b);
		      String line;
		      while ((line = in.readLine()) != null) {
		         System.out.println(line);
		      }
		      in.close();
		      return result;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    return "ERROR";

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    return "ERROR";
		}
	      
// This is what the code might have looked like using the standard HttpClient in Java 11,
// but Java EE does not seem to be that up-to-date.
//		
//		var uriPattern = "http://api.openweathermap.org/data/2.5/weather?q=city&APPID=4eb93d434ba32f3c222ec1c1544ef92e";
//		
//		// Using java.net.http.HttpClient because it is now part of Java 11
//		// and the standard way of doing things.
//		// https://openjdk.java.net/groups/net/httpclient/recipes.html
//		
//		var uri = uriPattern.replace("city", city);
//		
//		// Using synchronous client API for now because it's been a while since I have used
//		// servlets, and not sure how asynchronous logic works yet. If this were Akka, I would
//		// have no problem as it's inherently asynchronous and reactive.
//		
//		HttpClient client = HttpClient.newBuilder()
//				.version(Version.HTTP_1_1)
//				.followRedirects(Redirect.NORMAL)
//				.connectTimeout(Duration.ofSeconds(20))
//				//.proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
//				//.authenticator(Authenticator.getDefault())
//				.build();
//		
//		 HttpRequest request = HttpRequest.newBuilder()
//		          .uri(URI.create(uri))
//		          .build();
//		 
//		HttpResponse<String> response;
//		try {
//			response = client.send(request, BodyHandlers.ofString());
//			System.out.println(response.statusCode());
//			System.out.println(response.body());
//			
//			return response.body();
//
//		} catch (IOException | InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return "ERROR";
//		}
    }
}
