plugins {
    kotlin("multiplatform") version "1.8.0" // Adjust the version as necessary
    id("org.jetbrains.compose") version "1.3.0" // Adjust the version as necessary
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17" // Ensure compatibility with JDK 20
            }
        }
        withJava()
    }

    sourceSets {
        val commonTest by getting
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
