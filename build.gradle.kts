plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij") version "1.14.1"
    id("io.ktor.plugin") version "2.3.4"
}

dependencies {
    implementation("org.json:json:20231013")
    implementation("io.ktor:ktor-client-cio:2.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("io.ktor:ktor-client-websockets:2.3.8")
    implementation("javax.websocket:javax.websocket-api:1.1")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:1.9")
}

group = "com.zhz.bytedance.development_assistant_zhz"
version = "1.5.5"

application {
    mainClass.set("$group.$name.ApplicationKt")
}

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.5")
    type.set("IC") // Target IDE Platform
//    plugins.set(listOf("com.example.another-plugin:1.0"))
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
        sinceBuild.set("222")
        untilBuild.set("232.*")
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
