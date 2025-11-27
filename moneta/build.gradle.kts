plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("maven-publish")
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

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("moneta")
            description.set("Moneta – Kotlin Multiplatform money type")
            url.set("https://github.com/VoirDev/moneta")
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/VoirDev/moneta")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
