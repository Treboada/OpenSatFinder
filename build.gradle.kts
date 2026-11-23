// Plugins are declared here so a single version applies to every module.
// Application modules opt in with `alias(libs.plugins.<name>)`.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ktlint) apply false
}
