package net.kolotyluk.weather;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test Case Suite for WeatherServlet
 * 
 * <p>
 * Note: This test suite includes both unit and integration tests using the magic of JUnit 5 tags.
 * See also the documentation for maven-surefire-plugin and maven-failsafe-plugin on how these tags
 * are used. Integration Tests are run under Jetty, which is managed by Maven.
 * </p>
 * 
 * @author eric@kolotyluk.net
 *
 */
public class WeatherServletTests {
    
    @ParameterizedTest
    @ValueSource(strings = { "London", "Hong Kong" })
    @Tag("integration")
    void integration(String city) {
    			
		try {
			URL url = new URL("http://localhost:8080/weather");			
		    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("POST");
		    connection.setDoOutput(true);
		    (new OutputStreamWriter(connection.getOutputStream()))
		    	.append(String.format("city=%s", city))
		    	.flush();
		      
		    String result = new BufferedReader(new InputStreamReader(connection.getInputStream()))
	    			.lines()
	    			.parallel()
	    			.collect(Collectors.joining("\n"));
		     
		    assertNotNull(result);
		    System.out.println(result);
		    
		    assertTrue(result.contains(city));
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    }
    
    /**
     * Weather App Unit Test Suite
     * <p>
     * While this is a high level unit test, there are many lower level unit tests
     * that could be added here as you would expect for individual methods, error
     * handling, etc.
     * </P>
     * @param jsonFileName resource file containing seed json
     */
    @ParameterizedTest
    @ValueSource(strings = { "London.json" })
    @Tag("unit")
    void getResult(String jsonFileName)
    {
    	try
    	{
    	    String resourceName = '/' + getClass().getName().replace('.', '/') + '/' + jsonFileName;
    	    System.out.println("resourceName = " + resourceName);
    		URL jsonResource = getClass().getResource(resourceName);
    		
    		String json = new BufferedReader(new InputStreamReader(jsonResource.openStream()))
    				.lines()
    				.parallel()
    				.collect(Collectors.joining("\n"));
    		
			WeatherServlet weatherServlet = new WeatherServlet();
			
			String result = weatherServlet.getResultHtml(json);
			
			assertNotNull(result);
			
			System.out.println(result);
			
		}
    	catch (Exception e)
    	{
			e.printStackTrace();
		}
    }
    
    @ParameterizedTest
    @ValueSource(strings = { "London", "Hong Kong" })
    @Tag("unit")
    void getCityNameResult(String city)
    {
    	try
    	{
			WeatherServlet weatherServlet = new WeatherServlet();

        	String json = String.format("{\"name\":\"%s\"}", city);
			JSONObject resultJson = (JSONObject) new JSONParser().parse(json);
			
			String cityNameResult = weatherServlet.getCityNameResult(resultJson);
			
			assertEquals(cityNameResult, city);
			
			assertThrows(
					IllegalArgumentException.class,
					() -> { weatherServlet.getCityNameResult(null); },
					"");
			
		}
    	catch (ParseException e)
    	{
			// TODO handle this better
			e.printStackTrace();
		} catch (Exception e) {
			// TODO handle this better
			e.printStackTrace();
		}
    }
    
    @Test
    @Tag("unit")
    void getCityNameResultNullArgument()
    {
    	try
    	{
			WeatherServlet weatherServlet = new WeatherServlet();
			
			assertThrows(
				IllegalArgumentException.class,
				() -> weatherServlet.getCityNameResult(null)
			);
		}
    	catch (Throwable t)
    	{
			// TODO handle this better
			t.printStackTrace();
		}
    }
    
    // TODO implement similar tests for other result extraction methods....

}
