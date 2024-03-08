import java.io.FileInputStream
import java.util.*

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij") version "1.17.2"
    id("io.ktor.plugin") version "2.3.4"
//    id("org.jetbrains.intellij") version "1.14.1"
}


fun loadProperties(filename: String): Properties = Properties().apply {
    load(FileInputStream(filename))
}

dependencies {
    implementation("org.json:json:20231013")
    implementation("javax.websocket:javax.websocket-api:1.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.5")
    implementation("org.json:json:20231013")
}

group = "com.zhz.code_assistant"
version = "1.0.0"

application {
    mainClass.set("$group.$name.ApplicationKt")
}

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.2")
//    version.set("2022.2.5")
    type.set("IC")
//    type.set("IU")
    pluginName.set("开发助手Free")
    plugins.set(listOf("java"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<Jar>{
        manifest {
            attributes["Main-Class"] = "com.example.MainKt"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from({
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("")
//        sinceBuild.set("222")
//        untilBuild.set("232.*")
//        untilBuild.set("232.8660.185")
    }

    signPlugin {
        enabled = true
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    buildPlugin {
        enabled = true
    }

    publishPlugin {
        enabled = true
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        channels.set(listOf("stable"))
    }

    runIde {
        enabled = true
        environment("ENVIRONMENT", "LOCAL")
    }

    verifyPlugin {
        enabled = true
    }

    runPluginVerifier {
        enabled = true
    }
}
