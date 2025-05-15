buildscript {
    repositories {
        google()  // Make sure you have this in the repositories section
        mavenCentral()
        jcenter()
    }
    dependencies {

        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")

    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
   

}
