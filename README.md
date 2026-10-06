# SMART on FHIR Authorization Server

A monorepo containing a SMART on FHIR authorization server and a Java client application for testing SMART authorization flows.

## Components

### SMART on FHIR Server

Spring Boot authorization server implementing SMART on FHIR authorization functionality.

Features:
- SMART configuration discovery
- OAuth 2.0 Authorization Code flow
- PKCE with S256
- Standalone launch
- EHR launch
- Patient launch context
- Encounter launch context
- Refresh tokens and rotation
- Token introspection
- Granular SMART scopes
- Patient and encounter access control
- Observation search constraints
- _include and _revinclude protection

### SMART on FHIR Client

Spring Boot client used to test the SMART authorization flows against the authorization server.

## Project Structure

- `smart-on-fhir-client/` - SMART client application
- `smart-on-fhir-server/` - SMART authorization server
- `smart-on-fhir-client/client-tree.txt` - client structure documentation
- `smart-on-fhir-server/server-tree.txt` - server structure documentation

## Start the PostgreSQL Server

cd smart-on-fhir-server

docker compose up -d

`` This starts PostgreSQL 16 in a Docker container.


From the server directory:

``
cd smart-on-fhir-server
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
`` 

``text
http://localhost:8080
`` 

## Running the Client

``text
http://localhost:8081
`` 

From the client directory:

``powershell
cd smart-on-fhir-client
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
`` 

## Testing

The server contains automated tests covering authorization, SMART scopes, patient and encounter access, and Observation filtering.

The client currently compiles successfully but does not contain automated tests.

## Monorepo

The client and server are maintained together so that cloning or forking this repository provides both components.

