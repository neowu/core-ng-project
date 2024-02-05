import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java apply false
    idea apply false
}

defaultTasks("clean", "build")

subprojects {
    if (childProjects.isNotEmpty()) return@subprojects      // ignore parent project

    apply(plugin = "java")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
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
        jvmArgs("-XX:+EnableDynamicAgentLoading")
    }

    tasks.register("mkdir") {
        group = "build setup"
        description = "Create project directories."
        doLast {
            for ((_, sourceSet) in project.the<SourceSetContainer>().asMap) {
                for (dir in sourceSet.java.srcDirs) {
                    dir.mkdirs()
                }
                for (dir in sourceSet.resources.srcDirs) {
                    dir.mkdirs()
                }
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
}

apply(plugin = "check")

allprojects {
    apply(plugin = "idea")
    idea {
        module {
            outputDir = file("${rootDir}/build/${rootDir.toPath().relativize(projectDir.toPath())}/idea/production")
            testOutputDir = file("${rootDir}/build/${rootDir.toPath().relativize(projectDir.toPath())}/idea/test")
        }
    }
}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "latest"
}
