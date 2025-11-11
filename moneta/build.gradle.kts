plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("maven-publish")
    id("signing")
}

group = "dev.voir"
version = "1.0.0-alpha01"

kotlin {
    jvmToolchain(21)

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}


kotlin {
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        jvmMain.dependencies {

        }

        commonMain.dependencies {
        }

        iosMain.dependencies {
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
