plugins {
    application
}

application {
    mainClass.set("Main")
}

tasks.named<ProcessResources>("processResources") {
    if (Env.property(project, "env") != null) {
        val envResources = file("conf/${Env.property(project, "env")}/resources")
        assert(!envResources.exists())
        inputs.dir(envResources)
        from(envResources)
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

tasks.named<Test>("test") {
    inputs.files(fileTree("conf"))
}

tasks.named<Tar>("distTar") {
    archiveFileName = "${archiveBaseName.get()}.${archiveExtension.get()}"
}

tasks.named<Zip>("distZip") {
    enabled = false
}

tasks.named<CreateStartScripts>("startScripts") {
    defaultJvmOpts = listOf("-Dcore.webPath=APP_HOME_VAR/web", "-Dcore.appName=${applicationName}")

    doLast {
        App.replaceText(property("windowsScript") as File, "APP_HOME_VAR", "%APP_HOME%")
        App.replaceText(property("unixScript") as File, "APP_HOME_VAR/web", "'\$APP_HOME/web'")
    }
}

tasks.named<JavaExec>("run") {
    // assign all system properties to application, e.g. ./gradlew -Dkey=value :some-service:run
    for ((key, value) in System.getProperties()) {
        if (key != "user.dir" && key != "java.endorsed.dirs") {
            systemProperties[key as String] = value
        }
    }
}

tasks.named("mkdir") {
    doLast {
        mkdir("${projectDir}/conf/dev/resources")
    }
}

afterEvaluate {
    // split dependencies lib and app classes/bin/web to support layered docker image
    // to cache/reuse dependencies layer
    tasks.register("docker") {
        group = "distribution"
        dependsOn("installDist")
        doLast {
            val rootGroup = if (parent!!.depth > 0) parent!!.group else project.group
            project.sync {
                from(tasks.named<Sync>("installDist").get().destinationDir.toString()) {
                    exclude("lib/${rootGroup}.*.jar")
                    exclude("bin")
                    exclude("web")
                    into("dependency")
                }
                from(tasks.named<Sync>("installDist").get().destinationDir.toString()) {
                    include("lib/${rootGroup}.*.jar")
                    include("bin/**")
                    include("web/**")
                    into("app")
                }
                into("${buildDir}/docker/package")
            }
            if (project.file("docker/Dockerfile").exists()) {
                project.sync {
                    from(project.file("docker"))
                    into("${buildDir}/docker")
                    preserve {
                        include("package/**")
                    }
                }
            }
        }
    }
}
