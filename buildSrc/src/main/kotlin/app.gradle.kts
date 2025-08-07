plugins {
    application
}

application {
    mainClass = "Main"
}

tasks.named<ProcessResources>("processResources") {
    val env = project.properties["env"] // use gradlew -Penv=${env} to pass
    if (env != null) {
        val envResourceDir = file("conf/${env}/resources")
        if (!envResourceDir.exists()) throw Error("$envResourceDir does not exist")
        inputs.dir(envResourceDir)
        from(envResourceDir)
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

interface Context {
    @get:Inject
    val fs: FileSystemOperations
}

afterEvaluate {
    val context = project.objects.newInstance<Context>()
    val rootGroup = if (parent!!.depth > 0) parent!!.group else project.group
    val dockerDir = file("docker")
    val installDistDir = tasks.named<Sync>("installDist").get().destinationDir
    val dockerDestDir = layout.buildDirectory.dir("docker").get()

    // split dependencies lib and app classes/bin/web to support layered docker image
    // to cache/reuse dependencies layer
    tasks.register("docker") {
        group = "distribution"
        dependsOn("installDist")
        doLast {
            context.fs.sync {
                from(installDistDir) {
                    exclude("lib/${rootGroup}.*.jar")
                    exclude("bin")
                    exclude("web")
                    into("dependency")
                }
                from(installDistDir) {
                    include("lib/${rootGroup}.*.jar")
                    include("bin/**")
                    include("web/**")
                    into("app")
                }
                into(dockerDestDir.dir("package"))
            }
            if (dockerDir.resolve("Dockerfile").exists()) {
                context.fs.sync {
                    from(dockerDir)
                    into(dockerDestDir)
                    preserve {
                        include("package/**")
                    }
                }
            }
        }
    }
}
