package com.example.githubapi.service;

import com.example.githubapi.exception.UserNotFoundException;
import com.example.githubapi.models.Repository;
import com.example.githubapi.models.githubmodels.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GitHubServiceImplTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private GitHubServiceImpl gitHubService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a real WebClient.Builder and use it to create the WebClient
        WebClient.Builder webClientBuilder = WebClient.builder();
        webClient = spy(webClientBuilder.baseUrl("https://api.github.com").build());

        gitHubService = new GitHubServiceImpl(webClient);

        // Mock the WebClient chain
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void getUserRepositories_Success() {
        // Given
        String username = "testUser";
        String token = "testToken";
        GithubRepo repo1 = new GithubRepo("repo1", new GithubOwner("testUser"), false);
        GithubRepo repo2 = new GithubRepo("repo2", new GithubOwner("testUser"), false);
        GithubBranch branch1 = new GithubBranch("main", new GithubCommit("sha1"));
        GithubBranch branch2 = new GithubBranch("develop", new GithubCommit("sha2"));

        when(responseSpec.bodyToFlux(GithubRepo.class)).thenReturn(Flux.just(repo1, repo2));
        when(responseSpec.bodyToFlux(GithubBranch.class)).thenReturn(Flux.just(branch1, branch2));

        // When
        Mono<List<Repository>> result = gitHubService.getUserRepositories(username, token);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(repos -> {
                    if (repos.size() != 2) return false;
                    Repository firstRepo = repos.get(0);
                    return firstRepo.name().equals("repo1") &&
                            firstRepo.ownerLogin().equals("testUser") &&
                            firstRepo.branches().size() == 2 &&
                            firstRepo.branches().get(0).name().equals("main") &&
                            firstRepo.branches().get(1).name().equals("develop");
                })
                .verifyComplete();

        verify(webClient, times(3)).get(); // Once for repos, twice for branches
        verify(requestHeadersUriSpec, times(3)).uri(any(Function.class));
        verify(requestHeadersSpec, times(3)).headers(any());
        verify(requestHeadersSpec, times(3)).retrieve();
        verify(responseSpec, times(1)).bodyToFlux(eq(GithubRepo.class));
        verify(responseSpec, times(2)).bodyToFlux(eq(GithubBranch.class));
    }

    @Test
    void getUserRepositories_UserNotFound() {
        // Given
        String username = "nonExistentUser";
        String token = "testToken";

        when(responseSpec.bodyToFlux(GithubRepo.class))
                .thenReturn(Flux.error(new UserNotFoundException("User not found: " + username)));

        // When
        Mono<List<Repository>> result = gitHubService.getUserRepositories(username, token);

        // Then
        StepVerifier.create(result)
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    void getUserRepositories_EmptyResult() {
        // Given
        String username = "userWithNoRepos";
        String token = "testToken";

        when(responseSpec.bodyToFlux(GithubRepo.class)).thenReturn(Flux.empty());

        // When
        Mono<List<Repository>> result = gitHubService.getUserRepositories(username, token);

        // Then
        StepVerifier.create(result)
                .expectNext(List.of())
                .verifyComplete();
    }
}