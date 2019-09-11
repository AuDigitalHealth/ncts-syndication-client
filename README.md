[![CircleCI](https://circleci.com/gh/AuDigitalHealth/ncts-syndication-client/tree/master.svg?style=shield)](https://circleci.com/gh/AuDigitalHealth/ncts-syndication-client) [![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=au.gov.digitalhealth%3Ancts-syndication-client&metric=alert_status)](https://sonarcloud.io/dashboard?id=au.gov.digitalhealth%3Ancts-syndication-client) [![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=au.gov.digitalhealth%3Ancts-syndication-client&metric=coverage)](https://sonarcloud.io/dashboard?id=au.gov.digitalhealth%3Ancts-syndication-client) [![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=au.gov.digitalhealth%3Ancts-syndication-client&metric=security_rating)](https://sonarcloud.io/dashboard?id=au.gov.digitalhealth%3Ancts-syndication-client) [![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=au.gov.digitalhealth%3Ancts-syndication-client&metric=ncloc)](https://sonarcloud.io/dashboard?id=au.gov.digitalhealth%3Ancts-syndication-client) [![Javadocs](http://www.javadoc.io/badge/au.gov.digitalhealth/ncts-syndication-client-library.svg)](http://www.javadoc.io/doc/au.gov.digitalhealth/ncts-syndication-client-library)
# NCTS Syndication Client

The National Clinical Terminology Service (NCTS) at
https://www.healthterminologies.gov.au provides a syndication service based on
W3C Atom allowing registered users to automate release file downloads.

This project provides a Java library, with a CLI and Maven Plugin wrapper, to
the syndication service which helps automate this process.

The client is configured to download content to a target directory which it will
use as a cache it attempts to update. That is if you use the client multiple
times and specify the same directory, files will only be downloaded if they
match the categories specified and are either not present in the local directory
or the hash of the file in the local directory doesn't match the feed. If the
hash doesn't match the feed the local file will be deleted and downloaded - this
accounts for local corruption or a change to the file in the feed.

# Building the project

Install Maven and Java, clone the project, and simply execute Maven

```
mvn install
```

You'll now have the Maven Plugin in your local Maven repository, and in the
target directory you'll find the runnable jar generated with and without
dependencies built in.

There are 3 modules which get built

* the main Java library
* the runnable JAR file with a CLI wrapped around the library
* a Maven Plugin wrapped around the library

# Using the client

The client can be used from the command line or as a Maven Plugin. The typical
process is to

* Configure with a client id and client secret (required)
* Specify the syndication feed categories to download (at least one required)
* Specify if all files of the specified category(ies) should be downloaded, or
  just the latest (defaults to latest only)
* Specify the path to download the file(s) to (required for the Maven Plugin,
  defaults to the current directory for the CLI if not specified)

The URL for the syndication feed defaults to the production feed at
https://api.healthterminologies.gov.au/syndication/v1/syndication.xml, and the
token URL to get the bearer token with the client id and secret defaults to the
correct URL for this national feed. These can both be overidden if necessary.

For more information on the NCTS API security and an explanation of how to get
your client id and secret refer to
https://www.healthterminologies.gov.au/specs/v2/national-services/api-security

## Using the client library

The library can be downloaded from Maven Central as a dependency

```xml
<dependency>
    <groupId>au.gov.digitalhealth</groupId>
    <artifactId>ncts-syndication-client-library</artifactId>
    <version>1.1.0</version>
</dependency>
```

To use the client you first have to construct a SyndicationClient instance, and
provide it

* the feed URL
* the token URL
* the directory to download files to
* client id
* client secret

For convenience there is a SyndicationClient constructor that doesn't take the
feed and token URLs as parameters and defaults them to the correct values for
the NCTS.

An example use to download the latest SCT_RF2_FULL and all versions available
for SCT_RF2_SNAPSHOT and SCT_RF2_DELTA categories is below.

```java
SyndicationClient client = new SyndicationClient(
    new File("/path/to/syndication/download"),
    "insert client id",
    "insert client password");

client.download(false, "SCT_RF2_SNAPSHOT", "SCT_RF2_DELTA");
client.downloadLatest("SCT_RF2_FULL");
```

The download methods return a DownloadResult object which provides the path to
the file and a boolean indicator whether the file was actually downloaded or a
hit from the cache.

## Using the client as a Maven Plugin

The Maven client requires configuration to execute, specifically the client id
and secret, feed categories and the path to download to. Other parameters
default as above.

Currently this is not deployed to a public repository - but it will be...

A simple example is

```xml
<build>
  <plugins>
    <plugin>
      <groupId>au.gov.digitalhealth</groupId>
      <artifactId>ncts-syndication-client</artifactId>
      <version>1.1.0</version>
      <configuration>
        <outputDirectory>/tmp</outputDirectory>
        <categories>
          <param>SCT_RF2_FULL</param>
          <param>SCT_RF2_DELTA</param>
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
      <groupId>au.gov.digitalhealth</groupId>
      <artifactId>ncts-syndication-client</artifactId>
      <version>1.1.0</version>
      <configuration>
        <synd.url>https://api.healthterminologies.gov.au/syndication/v1/syndication.xml</synd.url>
        <synd.token.url>https://api.healthterminologies.gov.au/oauth2/token</synd.token.url>
        <outputDirectory>/tmp</outputDirectory>
        <categories>
          <param>SCT_RF2_FULL</param>
          <param>SCT_RF2_DELTA</param>
        </categories>
        <contentItemIds>
          <param>http://snomed.info/sct</param>
        </contentItemIds>
        <latestOnly>true</latestOnly>
        <clientId>YOUR CLIENT ID</clientId>
        <clientSecret>YOUR CLIENT SECRET</clientSecret>
      </configuration>
    <plugin>
  <plugins>
<build>
```

## Using the client from the command line

The project produces a runnable JAR file you can
[download from maven central here](https://search.maven.org/remotecontent?filepath=au/gov/digitalhealth/ncts-syndication-client-cli/1.1.0/ncts-syndication-client-cli-1.1.0-jar-with-dependencies.jar)
and run with

```
java -jar ncts-syndication-client-1.1.0-jar-with-dependencies.jar
```

The CLI version uses
[Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) and
generates help from the command line with the switch -? which is fairly self
explanatory.

```
usage: java
            au.gov.digitalhealth.ncts.syndication.client.DownloadSyndication
            Artefact
 -?,--help                       Prints the parameter usage help
 -category <arg>                 Category to download entries for, these
                                 appear in the term attribute of the
                                 category elements in the feed, for
                                 example 'SCT_RF2_FULL'
 -contentItemId <arg>            Content item identifier to download
                                 entries for, these appear in the
                                 contentItemIdentifier element in the
                                 feed, for example 'http://loinc.org'
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
