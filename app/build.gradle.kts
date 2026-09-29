import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

fun getLocalProperty(key: String): String? {
    val properties = Properties()
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        properties.load(file.inputStream())
    }
    return properties.getProperty(key)
}

android {

    signingConfigs {
        create("release") {
            storeFile = file(getLocalProperty("signing.storeFile")!!)
            storePassword = getLocalProperty("signing.storePassword")
            keyAlias = getLocalProperty("signing.keyAlias")
            keyPassword = getLocalProperty("signing.keyPassword")
        }
    }

    namespace = "top.stellortus.stellarmusic"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "top.stellortus.stellarmusic"
        minSdk = 24
        targetSdk = 37
        versionCode = 3
        versionName = "0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val versionName = output.versionName.get()
            val buildType = variant.buildType

            (output as com.android.build.api.variant.impl.VariantOutputImpl).outputFileName.set(
                "Stellar_Music_v${versionName}_${buildType}.apk"
            )
        }
    }
}


dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.nextoast)
    implementation(libs.androidx.navigation.compose)
}