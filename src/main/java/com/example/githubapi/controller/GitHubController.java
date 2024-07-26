package com.example.githubapi.controller;

import com.example.githubapi.exception.UserNotFoundException;
import com.example.githubapi.models.ErrorResponse;
import com.example.githubapi.models.Repository;
import com.example.githubapi.service.GitHubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/users/{username}/repositories")
    public Mono<ResponseEntity<Object>> getUserRepositories(
            @PathVariable String username,
            @RequestHeader("gitHub-Token") String token) {
        return gitHubService.getUserRepositories(username, token)
                .map(repositories -> ResponseEntity.ok().body((Object) repositories))
                .onErrorResume(UserNotFoundException.class, ex ->
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body((Object) new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage())))
                );
    }
}