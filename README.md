# Registration Authority using CMP Protocol

A simple registration authority(RA) website for user to signup and do certificate request. Administrator then can accept or reject the request. Tested working with CMP Interface of EJBCA Community. Setup the EJBCA to identify the website(CMP Client) as RA.

![](https://github.com/rsatrio/RegistrationAuthority-CMP/blob/main/RA-CMP.gif)

## Features
- REST Endpoint
- Authentication and Role Based Authorization with JWT
- Validation of POJO request with annotation
- Custom Appender for logging
- Dockerized
- Angular 12  Front End (modified version of https://github.com/erdkse/adminlte-3-angular) 
- Enroll Certificate and Revoke Certificate using CMP Protocol
- Administrator to review certificate registration and approve or reject it.

## Build

- Install nodejs 
- Run this to install required NPM and build angular AdminLTE:
```shell
cd src/ra-frontend
npm install
ng build --base-href=/ra/
```
- Use mvn package to build the module into jar file
- Run docker as below



## Docker
- Build the package as above in [Build]
- Go to the root directory of the project and run:
```shell
docker build -t ra-cmp .
```
- And then run the docker image:
```shell
docker run -p 9080:9080 ra-cmp
```
- Go to the browser and try to open the RA-CMP app in http://localhost:9080/ra/
- Register as regular user, or use admin account (admin1@test.com / password)
- In order to successfully issued certificate, we must connect to CA Provider that provide CMP interface. I used EJBCA, and set it up to use CMP Client as RA. 
- Change the "urlCmp" in config.yml pointing to available CMP Interface. 
- Change the "dnRoot" value in config.yml pointing to your CA distinguished name

## Feedback
For feedback, please raise issues in the issue section of the repository. Periodically, I will update the example with more real-life use case example. Enjoy!!.

