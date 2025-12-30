import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
    id("org.xbib.gradle.plugin.jflex") version "3.0.2"
}

group = "com.scriptrunner"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("kotlinc.path", "C:\\Program Files\\JetBrains\\IntelliJ IDEA 2025.2.4\\plugins\\Kotlin\\kotlinc\\bin\\kotlinc.bat")
}

compose.desktop {
    application {
        mainClass = "com.scriptrunner.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ScriptRunner"
            packageVersion = "1.0.0"

            windows {
                menuGroup = "Script Runner"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

// JFlex configuration
jflex {
    encoding = "UTF-8"
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generated/sources/main"))
    }
}

tasks.named("compileKotlin") {
    dependsOn("generateJflex")
}

tasks.named("compileJava") {
    dependsOn("generateJflex")
}
