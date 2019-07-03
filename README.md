# weather

Demo web-app for making weather queries

See also [Jetty and Maven Hello World](https://wiki.eclipse.org/Jetty/Tutorial/Jetty_and_Maven_HelloWorld)

# Running the App

    mvn clean compile mvn jetty:run

In a web browser enter the url

    localhost:8080

Select a city and click the `Submit` button.

To shutdown the jetty service, in the terminal window, enter ctrl-C

## WEATHER_APPID

Before running the service you need to define the WEATHER_APPID environment variable. For this
demo, please contact eric@kolotyluk.net for the correct appid.

For security reasons, you should never store secrets, such as an application identifier, in your source repository. These secrets should always be handled externally as part of professional DevOps
function.

For this demo we have chosen the simple practice of environment variables, but a commercial
deployment would probably use other practices.

### Linus and OSX

You can add the following to your .bash_profile

    setenv WEATHER_APPID <myappid>

### Windows 10

From the start menu,...

### Eclipse

When running Jetty from Eclipse, in the run configuration you will have to define the
environment variable WEATHER_APPID

