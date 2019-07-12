package net.kolotyluk.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ServiceConfigurationError;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Servlet implementation class WeatherServlet
 * 
 * @author eric@kolotyluk.net
 */
@WebServlet("/weather")
public class WeatherServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final String WEATHER_APPID = System.getenv().get("WEATHER_APPID");
       
    /**
     * <h1>WeatherServlet Constructor</h1>
     * Make sure our WEATHER_APPID environment variable is defined so that we can connect
     * to "http://api.openweathermap.org/data/2.5/weather"
     * <p>
     * There are probably better ways to handle this, but for this demo, this suffices.
     * 
     * @throws ServiceConfigurationError 
     * @see HttpServlet#HttpServlet()
     */
    public WeatherServlet() throws ServiceConfigurationError
    {
        super();
        
		if (WEATHER_APPID == null)
		{
			// TODO - Write a test for this
			throw new ServiceConfigurationError("WEATHER_APPID environment variable is not defined!");
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		response
			.getWriter()
			.append("GET: city = ")
			.append(request.getParameter("city")); //.append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		try
		{
			String jsonResult = getWeather(request.getParameter("city"));
			String htmlResult = getResultHtml(jsonResult);
			
			response.setContentType("text/html; charset=UTF-8");
			response.setContentLength(htmlResult.length());
			response
				.getWriter()
				.append(htmlResult);
		}
		catch (IllegalArgumentException e)
		{
			System.err.println(request);
			String message = String.format("Internal Programming Exception: request = %s", request);
			throw new ServletException(message, e);
		}
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
	 * @param json JSON result to convert to HTML
	 * @return
	 * @see https://openweathermap.org/current#name
	 */
	String getResultHtml(String json)
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
			
			String description = getWeatherDesciptionsResult(weatherJson);
			
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
		if (jsonObject == null)
			throw new IllegalArgumentException("jsonObject is null!");
		
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
		if (jsonObject == null)
			throw new IllegalArgumentException("jsonObject is null!");

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
		if (sysJson == null)
			throw new IllegalArgumentException("sysJson is null!");

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
		if (dateTime == 0)
			throw new IllegalArgumentException("dateTime is 0!");

		if (timeZone == null)
			throw new IllegalArgumentException("timeZone is null!");

		long localTime = dateTime * 1000;
		
	    final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
	    
	    sdf.setTimeZone(timeZone);
	   	    
	    return sdf.format(new Date(localTime));
	}
	
	
	double getTemperatureCelsiusResult(JSONObject mainJson) throws ParseException
	{
		if (mainJson == null)
			throw new IllegalArgumentException("mainJson is null!");

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
		if (sysJson == null)
			throw new IllegalArgumentException("sysJson is null!");

		if (when == null)
			throw new IllegalArgumentException("when is null!");

		if (timeZone == null)
			throw new IllegalArgumentException("timeZone is null!");

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
	
	String getWeatherDesciptionsResult(JSONArray weatherJson) throws ParseException
	{
		if (weatherJson == null)
			throw new IllegalArgumentException("weatherJson is null!");

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
	
	/**
	 * Get weather report from api.openweathermap.org
	 * 
	 * @param city
	 * @return json result
	 */
	String getWeather(String city)
	{
		if (city == null)
			throw new IllegalArgumentException("city == null");
		
		// Using simple old fashion Java HTTP Client
		// There are better ways of doing this, such as
		// https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html
		
		String urlString = String.format(
			"http://api.openweathermap.org/data/2.5/weather?q=%s&APPID=%s",
			city,
			WEATHER_APPID);
		
		try {
			URL url = new URL(urlString);
		    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("GET");
		    connection.setDoOutput(true);
		    BufferedReader bufferedReader =
		    	new BufferedReader(
		    		new InputStreamReader(
		    			connection.getInputStream()));
		    
		    String result = bufferedReader
	    			.lines()
	    			.parallel()
	    			.collect(Collectors.joining("\n"));
		    
		    bufferedReader.close();
		    
		    return result;

		}
		catch (MalformedURLException e)
		{
			// TODO handle this better
			e.printStackTrace();
		    return "ERROR";
		}
		catch (IOException e)
		{
			// TODO handle this better
			e.printStackTrace();
		    return "ERROR";
		}
    }
}
