# vehicle

Vehicle is an example web service demonstrating this technology stack:

* Spring Boot (1.5, 2.0 didn't work with Spring Security)
* JPA - Spring data
* REST-ful API + tests with Spring Web 
* Swagger for API-documentation
* Basic Auth with Spring Security

## Use case
You've got a fleet of devices/vehicles moving around and sending their positions in roughly regular intervals.
The service shall provide a REST API to receive, process and store the movement information.

### API functions
  * Receive one position per request. The position is uploaded for a certain vehicle.
  * get all sessions of a vehicle in correct ordering
  * get a single session as an ordered list of the received positions by timestamp
  * get the last position of a certain vehicle

### Data format
  * timestamp: long
  * latitude: double
  * longitude: double
  * heading: integer
  * session: string

### Properties & Requirements
  * Concurrent uploads by multiple vehicles must be supported. 
  * The received position data shall be stored internally on the back-end server.
  * The endpoints shall be protected by an appropriate authentication method.


## How to build and run the web-service:

Enter on command line in root-dir (with pom.xml):
`mvn spring-boot:run`

(I used the Spring [Source Toolsuite](https://spring.io/tools) for development, there it's just clicking `run as spring-boot app`)

### Security
**Minimalistic authentication**: I didn't even configure a permanent user: the password is printed out on startup like this:
`Using default security password: 12691182-28d6-45d2-a0a7-676537dc223a`

=> Basic Http-Auth then uses `user:12691182-28d6-45d2-a0a7-676537dc223a`

This would of course only be safe if the app was using Https - There's keystore in the app but it's not activated - overkill for this use case.

### Performance
The data model is optimized a little bit to avoid problems with large amounts of data by fetching a vehicle's sessions automatically but not its positions - these have to be explicitely requested.

## Usage of sample script:

    ./initService.sh {csv-file} {password as printed by the app}

Example:
    `./initService.sh data1.csv 12691182-28d6-45d2-a0a7-676537dc223a`


## API documentation

The app contains the default config for swagger, the UI allows you to explore the API:
http://localhost:8080/swagger-ui.html#/



