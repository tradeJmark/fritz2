import com.apurebase.kgraphql.GraphQL
import dev.fritz2.graphql.data.GitHubProjectDto
import dev.fritz2.graphql.data.TestGitHubProjectRepository
import dev.fritz2.graphql.data.TestProgrammingLanguageRepository
import dev.fritz2.graphql.types.GitHubProject
import dev.fritz2.graphql.types.ProgrammingLanguage
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 5000, host = "127.0.0.1") {
        install(GraphQL) {
            playground = true
            schema {
                query("hello") {
                    resolver { -> "World" }
                }

                query("languages") {
                    resolver { -> TestProgrammingLanguageRepository.getAll() }
                }

                query("projects") {
                    resolver { languageId: Int? ->
                        val projectDtos: List<GitHubProjectDto> = languageId?.let {
                            TestGitHubProjectRepository.getFiltered { it.languageId == languageId }
                        }?: run {
                            TestGitHubProjectRepository.getAll()
                        }

                        projectDtos.map {
                            GitHubProject(
                                it.id,
                                it.name,
                                it.url,
                                TestProgrammingLanguageRepository.getById(it.languageId)
                            )
                        }
                    }
                }

                type<ProgrammingLanguage>()
                type<GitHubProject>()
            }
        }
    }.start(wait = true)
}