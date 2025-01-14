import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import java.net.URL

// Configure project's dependencies
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij.platform") version "2.0.0-beta7"
//    id("org.jetbrains.intellij.platform.migration") version "2.0.0-beta7"
}

group = "com.wsl.symlinks"
version = "2024.2.1"


dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.2")
    implementation(kotlin("test"))
    implementation("org.codehaus.jettison:jettison:1.5.4")
    intellijPlatform {
            pluginVerifier()
            zipSigner()
            instrumentationTools()
            testFramework(TestFrameworkType.Platform)
            create(IntelliJPlatformType.IntellijIdeaUltimate, "242.19890.14")
        }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}


intellijPlatform {
    pluginConfiguration {
        name.set("WslSymlinks")
    }

    // Plugin Dependencies -> https://plugins.jetbrains.com/docs/intellij/plugin-dependencies.html
    // Example: platformPlugins = com.intellij.java, com.jetbrains.php:203.4449.22
    //
    // com.dmarcotte.handlebars: see https://plugins.jetbrains.com/plugin/6884-handlebars-mustache/versions
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    publishPlugin {
        token.set(System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken"))
    }

}


tasks.buildSearchableOptions {
    enabled = false
}


tasks.register("printVersion") {
    doLast { println(version) }
}


tasks.register("updateChangelog") {
    doLast {
        var input = generateSequence(::readLine).joinToString("\n")
        input = input.replace(":rocket:", "🚀")
        input = input.replace(":bug:", "🐛")
        input = input.replace(":documentation:", "📝")
        input = input.replace(":breaking:", "💥")
        input += "\nsee <a href=\"https://github.com/patricklx/intellij-wsl-symlinks/blob/main/CHANGELOG.md\">https://github.com/patricklx/intellij-wsl-symlinks/</a> for more"
        val f = File("./src/main/resources/META-INF/plugin.xml")
        var content = f.readText()
        content = content.replace("CHANGELOG_PLACEHOLDER", input)
        f.writeText(content)
    }
}

tasks.register("listRecentReleased") {
    doLast {
        val text = URL("https://plugins.jetbrains.com/api/plugins/24097/updates?channel=&size=8").readText()
        val obj = groovy.json.JsonSlurper().parseText(text)
        val versions = (obj as ArrayList<Map<*,*>>).map { it.get("version") }
        println(groovy.json.JsonBuilder(versions).toPrettyString())
    }
}

tasks.register("verifyAlreadyReleased") {
    doLast {
        var input = generateSequence(::readLine).joinToString("\n")
        val text = URL("https://plugins.jetbrains.com/api/plugins/24097/updates?channel=&size=100").readText()
        val obj = groovy.json.JsonSlurper().parseText(text)
        val versions = (obj as ArrayList<Map<*,*>>).map { it.get("version") }
        println(versions.contains(input))
    }
}
