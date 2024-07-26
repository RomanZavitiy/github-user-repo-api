package com.example.githubapi.models;

import java.util.List;

public record Repository(String name, String ownerLogin, List<Branch> branches) {}