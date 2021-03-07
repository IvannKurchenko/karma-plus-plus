#Karma++

https://karmaplusplus.com - Contribute to community and invoke `karma++;`
Aggregator of Github `help-wanted` issues, stack exchange questions, reddit posts based on your preferences.
Create feed via suggestions

## Stack
- Scala and cats-effect;
- Angular;

# Prerequisites
Necessary software required to be installed to work with.

- Java 8 (not sure about 9+ - Scala sometimes not very friendly with it);
- sbt 1.4 and Scala 2.13;
- Docker v 19+;
- npm v14.15.5;

# Hot to build frontend
Go to frontend directory
>cd karma-frontend

Download half of internet via
>npm install

Build Angular frontend app
>npm run build

Find build result at `karma-frontend/dist/karma-frontend`

# Hot to run frontend
>npm run build

Open in a browser  http://localhost:4200/

# Hot to compile application
>sbt clean compile  

# How to run application
>sbt run

Open in a browser http://localhost:8080

# How to build Docker container
Make sure you built Angular frontend first
> sbt docker:publishLocal

Find built image: `ikurchenko/karmaplusplus/karma-plus-plus:latest`
Which you can run with
> docker run -p 8080:8080 ikurchenko/karmaplusplus/karma-plus-plus

# Contribution
Any contribution to the project more than welcome: issues reports, feature suggestions etc.