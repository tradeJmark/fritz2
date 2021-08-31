import com.apurebase.kgraphql.GraphQL
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 5000, host = "0.0.0.0") {
        install(GraphQL) {
            playground = true
            schema {
                query("hello") {
                    resolver { -> "World" }
                }
            }
        }
    }.start(wait = true)
}