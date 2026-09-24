import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// La API key vive en local.properties, que Git ignora por defecto.
// Si falta, el build NO falla: la app compila y mostrara un error
// controlado al consultar (asi el docente puede clonar y compilar).
val localProperties = Properties().apply {
    val archivo = rootProject.file("local.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
val owmApiKey: String = localProperties.getProperty("OWM_API_KEY", "")

android {
    namespace = "ec.edu.uta.fisei.weatherviewer"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ec.edu.uta.fisei.weatherviewer"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "OWM_API_KEY", "\"$owmApiKey\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        animationsDisabled = true
        unitTests {
            isReturnDefaultValues = false
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    implementation(libs.kotlinx.coroutines.core)
// ...
    testImplementation(libs.mockwebserver)

    testImplementation(libs.junit)
    testImplementation(libs.org.json)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation(libs.kotlinx.coroutines.android)
}