import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
}

defaultTasks("clean", "build")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-proc:none", "-Werror"))
}

repositories {
    mavenCentral()
    mavenLocal()
}

layout.buildDirectory = file("$rootDir/build/${rootDir.toPath().relativize(projectDir.toPath())}")

tasks.named<Test>("test") {
    useJUnitPlatform()
    failFast = true
    testLogging.showStandardStreams = true
    testLogging.exceptionFormat = TestExceptionFormat.FULL

    var mockitoAgent: File? = configurations.testRuntimeClasspath.get().resolve().find { it.name.startsWith("mockito-core-") }
    if (mockitoAgent != null) {
        jvmArgs("-javaagent:${mockitoAgent}")
    }
}

tasks.register("mkdir") {
    group = "build setup"
    description = "Create project directories."

    val sourceDirs = project.the<SourceSetContainer>().flatMap { sourceSet ->
        sourceSet.java.srcDirs + sourceSet.resources.srcDirs
    }

    doLast {
        for (dir in sourceDirs) {
            dir.mkdirs()
        }
    }
}

afterEvaluate {
    // project.group/version is available on afterEvaluate as project.gradle is usually included at top
    if (parent!!.depth > 0) {
        group = "${group}.${parent!!.name}"    // resolve dependency collision of child projects with same name under different parents, result in unique artifactId
    }

    tasks.withType<Jar> {
        // resolve jar name collision of child projects with same name under different parents
        archiveBaseName = "${project.group}.${archiveBaseName.get()}"

        manifest {
            attributes["Implementation-Title"] = project.name
            attributes["Implementation-Version"] = project.version
            attributes["Created-By"] = "${System.getProperty("java.version")} (${System.getProperty("java.vendor")})"
            attributes["Built-With"] = "gradle ${project.gradle.gradleVersion}"
        }
    }
}
