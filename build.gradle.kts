import java.io.FileInputStream
import java.util.*
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML

fun properties(key: String) = providers.gradleProperty(key)

fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    alias(libs.plugins.kover) // Gradle Kover Plugin
}

kotlin {
    jvmToolchain(17)
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

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

repositories {
    mavenCentral()
    gradlePluginPortal()
}

//application {
//    mainClass.set("$group.$name.ApplicationKt")
//}


intellij {
//    version.set("2023.2")
//    version.set("2022.2.5")
//    type.set("IC")
//    type.set("IU")
//    pluginName.set("开发助手2023.2")
//    plugins.set(listOf("java"))


    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins = properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) }


}

changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

koverReport {
    defaults {
        xml {
            onCheck = true
        }
    }
}


tasks {

    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<Jar> {
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

//    patchPluginXml {
//        sinceBuild.set("232")
//        untilBuild.set("")
////        sinceBuild.set("222")
////        untilBuild.set("232.*")
////        untilBuild.set("232.8660.185")
//    }

    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")

        // Extract the <!-- Plugin description --> section from README-back.md and provide for the plugin's manifest
        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

//    runIdeForUiTests {
//        systemProperty("robot-server.port", "8082")
//        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
//        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
//        systemProperty("jb.consents.confirmation.enabled", "false")
//    }

    signPlugin {
        enabled = true
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        // token = "perm:MjMxMzE1NDkwMUBxcS5jb20=.OTItOTcxNw==.ROxg3r58vzHQhMsbjrvDAZwAO8aT0C"
        // channels = properties("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }
}
