plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "dev.voir"
version = "0.0.1"

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies { }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmMain.dependencies { }
        iosMain.dependencies { }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

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
                name.set("GNU Lesser General Public License, Version 3")
                url.set("https://www.gnu.org/licenses/lgpl-3.0.txt")
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
