# euregjug.eu

## About

This are the sources of euregjug.eu. The site is based on Spring and facilitates Spring Boot, Spring Data JPA and more really useful technologies. Find the annoucement [here](http://www.euregjug.eu/2016/01/07/new-site-is-live), a detailled readme respectivly blog post is on the way.

There's a lengthy blogpost about some of the stuff used here and how it's applied:

[Spring Boot based site and api for EuregJUG](http://info.michael-simons.eu/2016/01/14/spring-boot-based-site-and-api-for-euregjug)

About upgrades and new Spring Boot 1.4 features:

[EuregJUG.eu upgraded to Spring Boot 1.4](http://www.euregjug.eu/2016/07/29/euregjugeu-upgraded-to-spring-boot-14)

EuregJUG as a usecase for integration tests:

[Integration testing with Docker and Maven](http://info.michael-simons.eu/2016/08/25/integration-testing-with-docker-and-maven/)

## Compiling

This site is a maven project. The maven build needs [Docker](https://www.docker.com) for integration testing. Please install it for your platform before running

```
mvn clean install
```

That should be all.

## Running locally

When running from an IDE, the application expects a local SMTP server on port 25000 and a [MongoDB](https://www.mongodb.com) instance on the default port. If you use `mvn spring-boot:run` you also need a local [Redis](http://redis.io). To make your life easier, the build file provides those as docker images as well. The SMTP server is based on [Michael Simons' groovy-mocksmtp](https://github.com/michael-simons/java-mocksmtp/tree/master/groovy). To create those images use

```
mvn docker:build
```

Running and stopping them is as easy as

```
mvn docker:run
mvn docker:stop
```