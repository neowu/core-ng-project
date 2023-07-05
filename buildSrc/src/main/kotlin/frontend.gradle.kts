tasks.named("mkdir") {
    doLast {
        mkdir("${projectDir}/src/main/dist/web")
    }
}

afterEvaluate {
    assert(project.extensions.extraProperties.has("frontendDir"))
    val frontendDir = project.extensions.extraProperties.get("frontendDir") as String

    tasks.register("buildFrontend") {
        group = "build"
        doLast {
            assert(file(frontendDir).exists())

            exec {
                workingDir(frontendDir)
                commandLine(Frontend.commandLine(listOf("yarn", "install")))
            }

            val command = mutableListOf("yarn", "run", "build")

            val env = Env.property(project, "env")
            if (env != null) command.addAll(listOf("--env", env))

            exec {
                workingDir(frontendDir)
                commandLine(Frontend.commandLine(command))
            }

            delete("src/main/dist/web")
            copy {
                from("${frontendDir}/build/dist")
                if (env != null) exclude("static/*")    // exclude everything in static folder on server env, but keep static folder if exists
                into("src/main/dist/web")
            }
        }
    }
}
