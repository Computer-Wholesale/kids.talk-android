plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.navigation) apply false
    alias(libs.plugins.googleGmsServices) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.ossLicenses) apply false // Kids.Talk: KID-270 in-app OSS licenses screen
}