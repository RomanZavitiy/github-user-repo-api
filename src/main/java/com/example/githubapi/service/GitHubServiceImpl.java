package com.example.githubapi.service;

import com.example.githubapi.exception.UserNotFoundException;
import com.example.githubapi.models.Branch;
import com.example.githubapi.models.Repository;
import com.example.githubapi.models.githubmodels.GithubBranch;
import com.example.githubapi.models.githubmodels.GithubRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GitHubServiceImpl implements GitHubService {

    private final WebClient webClient;
    private static final Logger log = LoggerFactory.getLogger(GitHubServiceImpl.class);

    public GitHubServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<List<Repository>> getUserRepositories(String username, String token) {
        return getRepositories(username, token)
                .flatMap(repo -> getBranches(username, repo.name(), token)
                        .map(branches -> new Repository(repo.name(), repo.owner().login(), branches)))
                .collectList()
                .onErrorMap(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return new UserNotFoundException("User not found: " + username);
                    }
                    return ex;
                })
                .doOnSuccess(repos -> log.info("Fetched {} repositories for user: {}", repos.size(), username));
    }

    private Flux<GithubRepo> getRepositories(String username, String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/{username}/repos")
                        .queryParam("per_page", 100)
                        .build(username))
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .bodyToFlux(GithubRepo.class)
                .filter(repo -> !repo.fork())
                .doOnSubscribe(subscription -> log.info("Fetching repositories for user: {}", username))
                .doOnComplete(() -> log.info("Finished fetching repositories for user: {}", username));
    }

    private Mono<List<Branch>> getBranches(String username, String repoName, String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{username}/{repoName}/branches")
                        .queryParam("per_page", 100)
                        .build(username, repoName))
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .bodyToFlux(GithubBranch.class)
                .map(branch -> new Branch(branch.name(), branch.commit().sha()))
                .collectList()
                .doOnSuccess(branches -> log.info("Fetched {} branches for repo: {}/{}", branches.size(), username, repoName));
    }
}
