# NCTS Syndication Client
The National Clinical Terminology Service (NCTS) at https://www.healthterminologies.gov.au provides a syndication service based on W3C Atom allowing registered users to automate release file downloads.
This project provides a CLI and Maven Plugin client to the syndication service which helps automate this process.

# Building the project
Install Maven and Java, clone the project, and simply execute Maven
```
mvn install
```
You'll now have the Maven Plugin in your local Maven repository, and in the target directory you'll find the runnable jar generated with and without dependencies built in.

# Using the client
The client can be used from the command line or as a Maven Plugin. The typical process is to
 - Configure with a client id and client secret (required)
 - Specify the syndication feed categories to download (at least one required)
 - Specify if all files of the specified category(ies) should be downloaded, or just the latest (defaults to latest only)
 - Specify the path to download the file(s) to (required for the Maven Plugin, defaults to the current directory for the CLI if not specified)

The URL for the syndication feed defaults to the production feed at https://api.healthterminologies.gov.au/syndication/v1/syndication.xml, and the token URL to get the bearer token with the client id and secret defaults to the correct URL for this national feed. These can both be overidden if necessary.

For more information on the NCTS API security and an explanation of how to get your client id and secret refer to https://www.healthterminologies.gov.au/specs/v2/national-services/api-security

## Using the client as a Maven Plugin
The Maven client requires configuration to execute, specifically the client id and secret, feed categories and the path to download to. Other parameters default as above.

Currently this is not deployed to a public repository - but it will be...

A simple example is
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.dionmcm</groupId>
      <artifactId>ncts-syndication-client</artifactId>
      <version>0.0.1-SNAPSHOT</version>
      <configuration>
        <outputDirectory>/tmp</outputDirectory>
        <categories>
          <category>SCT_RF2_FULL</category>
          <category>SCT_RF2_DELTA</category>
        </categories>
        <clientId>YOUR CLIENT ID</clientId>
        <clientSecret>YOUR CLIENT SECRET</clientSecret>
      </configuration>
    <plugin>
  <plugins>
<build>
```

For the complete parameter set as an example
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.dionmcm</groupId>
      <artifactId>ncts-syndication-client</artifactId>
      <version>0.0.1-SNAPSHOT</version>
      <configuration>
        <synd.url>https://api.healthterminologies.gov.au/syndication/v1/syndication.xml</synd.url>
        <synd.token.url>https://api.healthterminologies.gov.au/oauth2/token</synd.token.url>
        <outputDirectory>/tmp</outputDirectory>
        <categories>
          <category>SCT_RF2_FULL</category>
          <category>SCT_RF2_DELTA</category>
        </categories>
        <latestOnly>true</latestOnly>
        <clientId>YOUR CLIENT ID</clientId>
        <clientSecret>YOUR CLIENT SECRET</clientSecret>
      </configuration>
    <plugin>
  <plugins>
<build>
```

## Using the client from the command line
The project produces a runnable JAR file you can run with
```
java -jar ncts-syndication-client-0.0.1-SNAPSHOT-jar-with-dependencies.jar 
```
The CLI version uses [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) and generates help from the command line with the switch -? which is fairly self explanatory.
```
usage: java
            com.github.dionmcm.ncts.syndication.client.DownloadSyndication
            Artefact
 -?,--help                       Prints the parameter usage help
 -category <arg>                 Category to download entries for, these
                                 appear in the term attribute of the
                                 category elements in the feed, for
                                 example 'SCT_RF2_FULL'
 -feed,--feed-url <arg>          URL of the NCTS syndication feed,
                                 defaults to
                                 https://api.healthterminologies.gov.au/sy
                                 ndication/v1/syndication.xml if not set
 -id,--client-id <arg>           Client id from the client credentials to
                                 use when authenticating to download
                                 entries
 -latest                         If provided, this option will cause the
                                 process to only download the latest entry
                                 (entry with the biggest content item
                                 version) from the feed for each category
                                 specified
 -out,--output-directory <arg>   Directory to download entries to
 -secret,--client-secret <arg>   Secret for the client id specified from
                                 the client credentials to use when
                                 authenticating to download entries
 -token,--token-url <arg>        URL of the NCTS token endpoint for
                                 authentication, defaults to
                                 https://api.healthterminologies.gov.au/oa
                                 uth2/token if not set
```
