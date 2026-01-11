plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "me.arcator"

version = "3.1"

description = "Assorted entity utils."

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props =
                mapOf(
                        "name" to project.name,
                        "version" to project.version,
                        "description" to project.description,
                        "apiVersion" to "1.21",
                )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") { expand(props) }
    }
}
