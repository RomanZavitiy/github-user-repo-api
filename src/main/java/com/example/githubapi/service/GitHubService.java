package com.example.githubapi.service;

import com.example.githubapi.models.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GitHubService {
    Mono<List<Repository>> getUserRepositories(String username, String token);
}
