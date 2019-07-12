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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

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

import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 
import org.json.simple.parser.*; 
import org.json.simple.parser.ParseException;

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
		response.setContentType("text/html; charset=UTF-8");
		response
		.getWriter()
		//.append("POST: city = ")
		//.append(request.getParameter("city"))
		//.append("\n\n\n")
		//.append(getWeather(request.getParameter("city")));
		.append(getResult(getWeather(request.getParameter("city"))));
		//.append(request.getContextPath());

	}
	
	/**
	 * {
	 * 	"coord":{"lon":-0.13,"lat":51.51},
	 * 	"weather":
	 * 		[
	 * 			{
	 * 				"id":803,
	 * 				"main":"Clouds",
	 * 				"description":"broken clouds",
	 * 				"icon":"04n"
	 * 			}
	 *		],
	 * 	"base":"stations",
	 * 	"main":
	 * 		{
	 * 			"temp":293.03,
	 * 			"pressure":1015,
	 * 			"humidity":43,
	 * 			"temp_min":290.37,
	 * 			"temp_max":295.93
	 * 		},
	 * 	"visibility":10000,
	 * 	"wind":
	 * 		{
	 * 			"speed":3.1,
	 * 			"deg":280
	 * 		},
	 * 	"clouds":
	 * 		{
	 * 			"all":54
	 * 		},
	 * 	"dt":1562362051,
	 * 	"sys":
	 * 		{
	 * 			"type":1,
	 * 			"id":1502,
	 * 			"message":0.0101,
	 * 			"country":"GB",
	 * 			"sunrise":1562298613,
	 * 			"sunset":1562357969
	 * 		},
	 * 	"timezone":3600,
	 * 	"id":2643743,
	 * 	"name":"London",
	 * 	"cod":200
	 * }
	 * 
	 * According to https://openweathermap.org/current#name the times are defined in unix, UTC 
	 * @param json
	 * @return
	 * @see https://openweathermap.org/current#name
	 */
	String getResult(String json)
	{
		String htmlFormat =
				"<!DOCTYPE html>" + 
				"<html>" + 
				"<head>" + 
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" + 
				"<title>Weather Demo</title>" + 
				"</head>" + 
				"<body>" + 
				"<h1>Weather in %s as of %s local time</h1>" +
				"<p>is %s</p>" +
				"<p>temperature:</p>" +
				"<ul>" +
				"<li>%.1f &#8457;</li>" +
				"<li>%.1f &#8451;</li>" +
				"</ul>" +
				"<p>sunrise: %s</p>" +
				"<p>sunset: %s</p>" +
				"</body>" + 
				"</html>";
		
		try
		{
			JSONObject resultJson = (JSONObject) new JSONParser().parse(json);
			JSONObject mainJson  = (JSONObject) resultJson.get("main");
			JSONObject sysJson = (JSONObject) resultJson.get("sys");
			JSONArray weatherJson = (JSONArray) resultJson.get("weather");
			
			String cityName = getCityNameResult(resultJson);
			long dateTime = getDateTimeResult(resultJson);
			
			String  country = getCountryResult(sysJson);
			
			TimeZone timeZone = TimeZone.getTimeZone(country);
			String localtime = getLocalTimeResult(dateTime, timeZone);
			
			double tempCelsius = getTemperatureCelsiusResult(mainJson);
			double tempFahrenheit = tempCelsius * 9 / 5 + 32;
			
			String sunrise = getSunResult(sysJson, "sunrise", timeZone);
			String sunset = getSunResult(sysJson, "sunset", timeZone);
			
			String description = getWeatherResults(weatherJson);
			
			return String.format(htmlFormat, cityName, localtime, description, tempFahrenheit, tempCelsius, sunrise, sunset);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			return json;
		} 
	}
	
	String getCityNameResult(JSONObject jsonObject) throws ParseException
	{
		String name = (String) jsonObject.get("name");
		
		if (name == null)
		{
			throw new ParseException(0, 0, "Parsed field 'name' is null!");
		}
		else
		{
			return name;
		}
	}
	
	long getDateTimeResult(JSONObject jsonObject) throws ParseException
	{
		Long dateTime = (Long) jsonObject.get("dt");
		
		if (dateTime == null)
		{
			throw new ParseException(0, 0, "Parsed field 'dt' is null!");
		}
		else
		{
			return dateTime;
		}
	}
	
	String getCountryResult(JSONObject sysJson) throws ParseException
	{
		String country = (String) sysJson.get("country");
		
		if (country == null)
		{
			throw new ParseException(0, 0, "Parsed field 'country' is null!");
		}
		else
		{
			return country;
		}
	}

	
	String getLocalTimeResult(long dateTime, TimeZone timeZone)
	{
		long localTime = dateTime * 1000;
		
	    final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
	    
	    sdf.setTimeZone(timeZone);
	   	    
	    return sdf.format(new Date(localTime));
	}
	
	
	double getTemperatureCelsiusResult(JSONObject mainJson) throws ParseException
	{
		Double temperatureCelsius = (Double) mainJson.get("temp");
		
		if (temperatureCelsius == null)
		{
			throw new ParseException(0, 0, "Parsed field 'temp' is null!");
		}
		else
		{
			return temperatureCelsius - 273.15;
		}
	}

	
	String getSunResult(JSONObject sysJson, String when, TimeZone timeZone) throws ParseException
	{
		Long sunWhen = (Long) sysJson.get(when);
		
		if (sunWhen == null)
		{
			throw new ParseException(0, 0, "Parsed field '" + when + "' is null!");
		}
		else
		{
			long localTime = sunWhen * 1000;
			
		    final DateFormat sdf = new SimpleDateFormat("hh:mm:ss aa");
		    // final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    
		    sdf.setTimeZone(timeZone);
			
		    return sdf.format(new Date(localTime));
		}
	}
	
	String getWeatherResults(JSONArray weatherJson) throws ParseException
	{
		// Argh! Functional programming is so much easier in Scala and Kotlin...
		@SuppressWarnings("unchecked")
		Stream<String> descriptions =
			weatherJson.stream()
				.map((Object object) -> ((JSONObject) object).get("description"));
		String result = descriptions.reduce("", (a, b) -> a + b + ", ");
		
		if (result == null)
		{
			throw new ParseException(0, 0, "Parsed field 'description' is null!");
		}
		else
		{
			return result;
		}
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
