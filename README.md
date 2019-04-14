# README

## Json Result

To optimize json occupation, i use a separate data bloc for Flight data and combinaison. Combinaison contains only 
flight ids (to go and return) and flight data contains only information about combinaison ids. Flight data has an 
unique occurence, that it be used for many combinaisons.

    {
      "departure": "CDG",
      "arrival": "LHR",
      "departureAt": "2019-03-21",
      "returnAt": "2019-03-23",
      "tripType": "R",
      "flights": [
        {
          "price": 380,
          "id": "28641c6b-3d9c-4163-9820-8a7b5d5fe44f",
          "departure": "CDG",
          "arrival": "LHR",
          "departureAt": "2019-03-21T15:49:00",
          "arrivalAt": "2019-03-21T22:49:00"
        },
        …
      ],
      "groups": [
        {
          "price": 1161,
          "combinations": [
            {
              "departureId": "28641c6b-3d9c-4163-9820-8a7b5d5fe44f",
              "returnId": "a02d1d90-f05c-4239-bc42-7beaf87e0741"
            },
            {
              "departureId": "28641c6b-3d9c-4163-9820-8a7b5d5fe44f",
              "returnId": "a67c2091-c3de-46c1-aed0-52dc256aa41b"
            },
            …
          ]
        },
        …
      ]
    }
  



## Build

This code use Java 11.

Run this command and build the application

    ./mvnw clean package

This command does: 

* download dependencies
* compile / generate Java code
* run tests
* package an executable jar


## Run

After building the application, run the command

    java -jar target bcm-0.0.1-SNAPSHOT.jar
    
and the application responds to [http://localhost:8080](http://localhost:8080/api/flights?departure_airport=CDG&arrival_airport=LHR&departure_date=2019-03-21&return_date=2019-03-23&tripType=R)

## Bonus

### CI / CD

#### CI

I could use a build pipeline (with Travis CI for exemple) to build application, run tests and build/push a docker image ready to deploy.

#### CD

For production, i could use a container orchestrator (Kubernates) and [Twelve factors](https://12factor.net/) to run the application.

