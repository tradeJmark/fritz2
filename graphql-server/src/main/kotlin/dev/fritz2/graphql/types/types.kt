package dev.fritz2.graphql.types

/**
 * Types used in the graphql api
 */

data class ProgrammingLanguage(
    val id: Int,
    val name: String,
)

data class GitHubProject(
    val id: Int,
    val name: String,
    val url: String,
    val language: ProgrammingLanguage?
)