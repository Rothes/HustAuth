plugins {
    id("java")
    id("edu.sc.seis.launch4j") version "3.0.5"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.rothes"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://jitpack.io")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    implementation("org.apache.logging.log4j:log4j-core:2.22.1")
    implementation(project(":log4j-ext"))
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
//    implementation("org.apache.httpcomponents.client5:httpclient5:5.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.Carleslc.Simple-YAML:Simple-Yaml:1.8.4")
}

tasks.shadowJar {
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer())
    minimize {
        include(dependency("org.apache.httpcomponents:.*:.*"))
        include(dependency("com.google.code.gson:.*:.*"))
        include(dependency("com.github.Carleslc.Simple-YAML:.*:.*"))
    }
}

launch4j {
    setJarTask(tasks.shadowJar.get())
    mainClassName = "io.github.rothes.hustauth.Main"
//    headerType = "console"
    jreMinVersion = "8"
    windowTitle = "HustAuth"
    maxHeapSize = 13
}

tasks.createExe {
    doLast {
        delete("${layout.buildDirectory.get()}/${launch4j.outputDir.get()}/${launch4j.libraryDir.get()}")
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
    sourceCompatibility = "8"
    targetCompatibility = "8"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.github.rothes.hustauth.Main"
        attributes["Multi-Release"] = true
    }
}

tasks.test {
    useJUnitPlatform()
}