tasks.named("mkdir") {
    doLast {
        mkdir("${projectDir}/src/main/dist/web")
    }
}

interface Context {
    @get:Inject
    val exec: ExecOperations

    @get:Inject
    val fs: FileSystemOperations
}

afterEvaluate {
    if (!project.extensions.extraProperties.has("frontendDir")) throw Error("project does not have frontendDir property, assign by project.ext[\"frontendDir\"]")

    val frontendDir = file(project.extensions.extraProperties.get("frontendDir") as String)
    if (!frontendDir.exists()) throw Error("$frontendDir does not exist")

    val context = project.objects.newInstance<Context>()
    val env = project.properties["env"] as String? // use gradlew -Penv=${env} to pass

    tasks.register("buildFrontend") {
        group = "build"
        doLast {
            context.exec.exec {
                workingDir(frontendDir)
                commandLine(Frontend.commandLine(listOf("pnpm", "install")))
            }

            val command = mutableListOf("pnpm", "run", "build")
            if (env != null) command.addAll(listOf("--env", env))

            context.exec.exec {
                workingDir(frontendDir)
                commandLine(Frontend.commandLine(command))
            }
            context.fs.delete {
                delete("src/main/dist/web")
            }
            context.fs.copy {
                from("${frontendDir}/build/dist")
                if (env != null) exclude("static/*")    // exclude everything in static folder on server env, but keep static folder if exists
                into("src/main/dist/web")
            }
        }
    }
}
