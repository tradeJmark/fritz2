plugins {
    application
    kotlin("jvm")
    id("com.github.psxpaul.execfork") version "0.1.15"
}

application {
    mainClass.set("dev.fritz2.graphql.server.ApplicationKt")
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

val graphqlServerKtorVersion = "1.6.2"

dependencies {
    implementation("io.ktor:ktor-server-core:$graphqlServerKtorVersion")
    implementation("io.ktor:ktor-server-netty:$graphqlServerKtorVersion")
    testImplementation("io.ktor:ktor-server-tests:$graphqlServerKtorVersion")
    implementation("ch.qos.logback:logback-classic:${rootProject.ext["logbackVersion"]}")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${rootProject.ext["kotlinVersion"]}")

    // graphql-support:
    implementation("com.apurebase:kgraphql:${rootProject.ext["kGraphqlVersion"]}")
    implementation("com.apurebase:kgraphql-ktor:${rootProject.ext["kGraphqlVersion"]}")
}
