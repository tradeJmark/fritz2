package dev.fritz2.graphql.data

import dev.fritz2.graphql.types.ProgrammingLanguage


data class GitHubProjectDto(
    val id: Int,
    val name: String,
    val url: String,
    val languageId: Int
)


interface ProgrammingLanguageRepository {
    fun getAll(): List<ProgrammingLanguage>
    fun getById(id: Int): ProgrammingLanguage?
}

interface GitHubProjectRepository {
    fun getAll(): List<GitHubProjectDto>
    fun getFiltered(predicate: (GitHubProjectDto) -> Boolean): List<GitHubProjectDto>
}


object TestProgrammingLanguageRepository : ProgrammingLanguageRepository {
    private val languages = listOf(
        ProgrammingLanguage(0, "Java"),
        ProgrammingLanguage(1, "Kotlin"),
        ProgrammingLanguage(2, "Python"),
        ProgrammingLanguage(3, "C"),
    )

    override fun getAll(): List<ProgrammingLanguage> = languages

    override fun getById(id: Int): ProgrammingLanguage? = languages.firstOrNull { it.id == id }
}

object TestGitHubProjectRepository : GitHubProjectRepository {
    private val repos: List<GitHubProjectDto> = listOf(
        GitHubProjectDto(0, "spring-boot", "https://github.com/spring-projects/spring-boot", 0),
        GitHubProjectDto(1, "spring-framework", "https://github.com/spring-projects/spring-framework", 0),
        GitHubProjectDto(2, "compose-jb", "https://github.com/spring-projects/spring-framework", 1),
        GitHubProjectDto(3, "fritz2", "https://github.com/jwstegemann/fritz2", 1),
        GitHubProjectDto(4, "ansible", "https://github.com/ansible/ansible", 2),
        GitHubProjectDto(5, "borg", "https://github.com/borgbackup/borg", 3),
    )

    override fun getAll(): List<GitHubProjectDto> = repos

    override fun getFiltered(predicate: (GitHubProjectDto) -> Boolean): List<GitHubProjectDto> = repos.filter(predicate)
}