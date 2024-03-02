import java.util.*
import java.io.FileInputStream

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij") version "1.14.1"
    id("io.ktor.plugin") version "2.3.4"
}

//val env = environment("env").getOrNull()

fun loadProperties(filename: String): Properties = Properties().apply {
    load(FileInputStream(filename))
}

//fun properties(key: String): Provider<String> {
//    if ("win-arm64" == env) {
//        val property = loadProperties("gradle-win-arm64.properties").getProperty(key)
//            ?: return providers.gradleProperty(key)
//        return providers.provider { property }
//    }
//    return providers.gradleProperty(key)
//}
//
//fun environment(key: String) = providers.environmentVariable(key)


dependencies {
    implementation("org.json:json:20231013")
    implementation("io.ktor:ktor-client-cio:2.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("io.ktor:ktor-client-websockets:2.3.8")
    implementation("javax.websocket:javax.websocket-api:1.1")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:1.9")
}

group = "com.zhz.bytedance.development_assistant_zhz"
version = "1.6.0"

application {
    mainClass.set("$group.$name.ApplicationKt")
}

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.1")
    type.set("IC")
    pluginName.set("CodeAssistant")
    plugins.set(listOf("java"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
