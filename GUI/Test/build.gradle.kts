import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "org.example"
version = "1.0-SNAPSHOT"

//repositories {
//    mavenCentral()
//    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
//    google()
//}
//
//dependencies {
//    implementation(compose.desktop.currentOs)
//    implementation("be.tarsos:tarsosdsp:2.4") // Add TarsosDSP dependency for audio processing
//}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven {
        name = "TarsosDSP repository"
        url = uri("https://mvn.0110.be/releases")
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("be.tarsos.dsp:core:2.5")
    implementation("be.tarsos.dsp:jvm:2.5")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Test"
            packageVersion = "1.0.0"
        }
    }
}