package net.kolotyluk.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class WeatherServletTests {

    //private final Calculator calculator = new Calculator();

    @Test
    @Disabled
    void addition() {
        assertEquals(2, 2);
    }
    
    @Test
    @Tag("integration")
    void integration() {
        assertEquals(2, 2);
    }
    
    @ParameterizedTest
    @ValueSource(strings = { "London.json" })
    @Tag("unit")
    void getCityNameResult(String jsonFileName)
    {
    	try {
    	    String resourceName = '/' + getClass().getName().replace('.', '/') + '/' + jsonFileName;
    	    System.out.println("resourceName = " + resourceName);
    		URL jsonResource = getClass().getResource(resourceName);
    		
    		String json = new BufferedReader(new InputStreamReader(jsonResource.openStream()))
    				.lines()
    				.parallel()
    				.collect(Collectors.joining("\n"));
    		
			WeatherServlet weatherServlet = new WeatherServlet();
			
			String result = weatherServlet.getResult(json);
			
			assertNotNull(result);
			
			System.out.println(result);
			
			//weatherServlet.getCityNameResult(sysJson)
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
