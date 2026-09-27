plugins {
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    id("com.vanniktech.maven.publish") version "0.36.0"
}

kotlin {
    explicitApi()
    jvmToolchain(21)

    jvm()
    android {
        namespace = "dev.voir.moneta"
        compileSdk = 37
        minSdk = 23
    }
    iosX64()
    iosArm64()
    macosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.voir.decimal)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

val isLocalPublish = gradle.startParameter.taskNames.any {
    it.contains("publishToMavenLocal", ignoreCase = true)
}

mavenPublishing {
    publishToMavenCentral()
    if (!isLocalPublish) {
        signAllPublications()
    }

    coordinates(
        groupId = "dev.voir",
        artifactId = "moneta",
        version = project.version.toString()
    )

    pom {
        name.set("Moneta – Kotlin Multiplatform money type")
        description.set("A Kotlin Multiplatform money type designed for safe, precise, and expressive monetary operations across iOS, JVM, and Android.")
        url.set("https://github.com/VoirDev/moneta/")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("checksanity")
                name.set("Gary Bezruchko")
                email.set("hello@voir.dev")
                organization.set("VOIR")
                organizationUrl.set("https://voir.dev")
            }
        }

        scm {
            url.set("https://github.com/VoirDev/moneta/")
            connection.set("scm:git:git://github.com/VoirDev/moneta.git")
            developerConnection.set("scm:git:ssh://git@github.com/VoirDev/moneta.git")
        }
    }
}
