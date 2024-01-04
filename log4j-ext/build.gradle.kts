plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-core:2.22.1")
    annotationProcessor("org.apache.logging.log4j:log4j-core:2.22.1")
}