# GitHub Repository API

This Spring Boot application provides a reactive API to fetch GitHub repositories for a given user.

## Features

- Fetch non-fork repositories for a given GitHub username
- Retrieve branch information for each repository
- Reactive implementation using Spring WebFlux
- Error handling for non-existent users

## Technologies

- Java 21
- Spring Boot 3.x
- Spring WebFlux
- Project Reactor
- WebClient for API calls

## Prerequisites

- JDK 21
- Maven
- GitHub Personal Access Token to be provided in header of endpoint.

Build the project using maven: mvn clean install and start GitHubApiApplication class.

The API will be available at `http://localhost:8080`.

## API Endpoints

### Get User Repositories

### http://localhost:8080/api/github/users/{username}/repositories

#### Parameters

- `username`: GitHub username

#### Headers

- `gitHub-Token`: generated GitHub user token

#### Response

```json
[
  {
    "repositoryName": "repository-name",
    "ownerLogin": "owner-username",
    "branches": [
      {
        "name": "branch-name",
        "lastCommitSha": "commit-sha"
      }
    ]
  }
]
```
#### Error Response
```json
{
  "status": 404,
  "message": "User not found: username"
}
```
