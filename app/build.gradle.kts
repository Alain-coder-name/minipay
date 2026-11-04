plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.minipay"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.minipay"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

// Automatic Fix Task: Deletes duplicate resources and clears emulator locks/processes
tasks.register("autoFix") {
    doLast {
        val resDir = projectDir.resolve("src/main/res")
        
        // 1. Delete duplicate drawable
        val dup1 = resDir.resolve("drawable/ic_launcher_foreground.png")
        if (dup1.exists()) {
            println("DELETING_FILE: ${dup1.absolutePath}")
            dup1.delete()
        }

        // 2. Delete duplicate mipmaps
        val mipmapDir = resDir.resolve("mipmap-xxxhdpi")
        listOf("ic_launcher.png", "ic_launcher_round.png").forEach { name ->
            val f = mipmapDir.resolve(name)
            if (f.exists()) {
                println("DELETING_FILE: ${f.absolutePath}")
                f.delete()
            }
        }

        // 3. Kill emulator and ADB processes on Windows
        try {
            println("Attempting to kill stuck emulator and ADB processes...")
            Runtime.getRuntime().exec("taskkill /F /IM qemu-system-x86_64.exe /T")
            Runtime.getRuntime().exec("taskkill /F /IM emulator.exe /T")
            Runtime.getRuntime().exec("taskkill /F /IM adb.exe /T")
        } catch (e: Exception) {
            println("Note: Could not kill processes automatically")
        }

        // 4. Delete emulator lock files
        val userHome = System.getProperty("user.home")
        val avdDir = file("${userHome}/.android/avd/Small_Phone.avd")
        if (avdDir.exists()) {
            println("Clearing locks in: ${avdDir.absolutePath}")
            avdDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".lock")) {
                    println("DELETING_LOCK: ${file.absolutePath}")
                    file.deleteRecursively()
                }
            }
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}