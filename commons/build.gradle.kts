plugins {
    id("connector.common-conventions")
}

dependencies {
    api(project(":api"))
    compileOnly(libs.bungeecord)
    compileOnly(libs.commandflow.common)
    implementation(libs.socketio)
    implementation(libs.caffeine)
    implementation(libs.jackson.databind)

    implementation("org.spongepowered:configurate-yaml:4.2.0")
}
