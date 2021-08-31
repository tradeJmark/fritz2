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

dependencies {
    implementation("io.ktor:ktor-server-core:${rootProject.ext["ktorVersion"]}")
    implementation("io.ktor:ktor-server-netty:${rootProject.ext["ktorVersion"]}")
    implementation("ch.qos.logback:logback-classic:${rootProject.ext["logbackVersion"]}")
    testImplementation("io.ktor:ktor-server-tests:${rootProject.ext["ktorVersion"]}")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${rootProject.ext["kotlinVersion"]}")
}
