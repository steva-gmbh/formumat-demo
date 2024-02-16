import org.apache.tools.ant.taskdefs.condition.Os
import java.util.*

val localprops = Properties().apply {
    try {
        load(File("local.properties").inputStream())
    } catch (ignored: Throwable) {
    }
}

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget()

    if (Os.isFamily(Os.FAMILY_MAC)) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "shared"
                isStatic = true
            }
        }
    }

    jvm("desktop")

    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.6")
                implementation("org.reduxkotlin:redux-kotlin-threadsafe:0.6.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                if (localprops.getProperty("formumat-public.dir") != null) {
                    implementation(project(":formumat-public"))
                } else {
                    implementation("digital.steva:formumat-public:0.0.1")
                }
                if (localprops.getProperty("expressions.dir") != null) {
                    implementation(project(":expressions"))
                } else {
                    implementation("io.github.murzagalin:multiplatform-expressions-evaluator:0.15.0")
                }
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.7.2")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.1")
            }
        }
        if (Os.isFamily(Os.FAMILY_MAC)) {
            val iosX64Main by getting
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting
            val iosMain by creating {
                dependsOn(commonMain)
                iosX64Main.dependsOn(this)
                iosArm64Main.dependsOn(this)
                iosSimulatorArm64Main.dependsOn(this)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
            }
        }

        val jsMain by getting {
            dependencies {
            }
        }
    }

    targets.forEach { target ->
        target.compilations.forEach { compilation ->
            compilation.kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "digital.steva.formumat.demo.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
