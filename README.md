# weather

Demo web-app for making weather queries

See also [Jetty and Maven Hello World](https://wiki.eclipse.org/Jetty/Tutorial/Jetty_and_Maven_HelloWorld)

# Running the App
In a terminal window, in the project directory, for example Git-Shell:

    mvn clean compile mvn jetty:run

In a web browser enter the url

    localhost:8080

Select a city and click the `Submit` button.

To shutdown the jetty service, in the terminal window, enter ctrl-C. You may need to do this a few times to get a response

    [INFO] Started Jetty Server
    <ctrl-C><ctrl-C>
	[INFO] Stopped ServerConnector@1cc93da4{HTTP/1.1,[http/1.1]}{0.0.0.0:8080}
	[INFO] node0 Stopped scavenging
	[INFO] Stopped o.e.j.m.p.JettyWebAppContext@2c2e3460{weather,/,file:///C:/Users/ERIC/Documents/git/repos/weather/src/main/webapp/,UNAVAILABLE}{file:///C:/Users/ERIC/Documents/git/repos/weather/src/main/webapp/}
	[INFO] Jetty server exiting.
	[INFO] ---------Terminate batch job (Y/N)? y
	C:\Users\ERIC\Documents\git\repos\weather [master ≡ +0 ~2 -0 | +3 ~2 -2 !]>



## WEATHER_APPID

Before running the service you need to define the WEATHER_APPID environment variable. For this
demo, please contact eric@kolotyluk.net for the correct appid.

For security reasons, you should never store secrets, such as an application identifier, in your
source repository. These secrets should always be handled externally as part of a professional DevOps
function.

For this demo we have chosen the simple practice of environment variables, but a commercial
deployment would probably use other practices.

### Linus and OSX

You can add the following to your .bash_profile

    setenv WEATHER_APPID <myappid>

Don't forget to restart your command line prompt to get the new environment settings.

### Windows 10

From the start menu, and select options (looks like a gear). Search for `environment`.
Select on of the `edit environment` choices, and add the environment variable.

Don't forget to restart your command line prompt to get the new environment settings.

## Jetty Maven Plugin

The easiest way to test your web app from Maven is to use the jetty-maven-plugin

    <properties>
      <jettyVersion>9.4.19.v20190610</jettyVersion>
    </properties>

    . . .
    <plugin>
      <groupId>org.eclipse.jetty</groupId>
      <artifactId>jetty-maven-plugin</artifactId>
      <version>${jettyVersion}</version>
    </plugin>
    . . .

Note, while you can run jetty under Java 9 or later, Jetty cannot handle class files newer than Java 8,
so you will need to build with a target of Java 8.

# Testing the App

## Unit Tests

    mvn clean test

## Integration Tests

    mvn clean verify

# Eclipse IDE

It is recommended you use this project with the [Eclipse IDE](https://www.eclipse.org).

## Jetty Eclipse Plugin

From the Eclipse menu Help -> Eclipse Marketplace... find `jetty` and click `go`. 
Install the latest version of `Eclipse Jetty`.

Note, while you can run jetty under Java 9 or later, Jetty cannot handle class files newer than Java 8,
so you will need to build with a target of Java 8.

Note: this method seems to have various problems...

1. java.lang.NoClassDefFoundError: org/json/simple/parser/ParseException

## Running the webapp

When running Jetty from Eclipse, in the run configuration you may have to define the environment variable
WEATHER_APPID. 

# Editorial Notes

## Details

This demo focuses on structure rather than details. Consequently, such details are left as an exercise
for the reader.

For example, while it incorporates both Unit and Integration tests,
it does not go into a lot of detailed coverage or completeness.

Similarly, it does not do UI testing via Selenium (or other tools).

## Performance & Scalability

This demo uses old-style Java EE technology, which does not necessarily have the best
performance and scalability characteristics. A more modern web app, front-end & back-end,
would use more performant techniques such as [Reactive](https://www.reactivemanifesto.org/)
Programming methods.

Frameworks such as [Akka](https://akka.io) tend to be more performant than Java EE,
delivering better price performance characteristics.
