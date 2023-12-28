tasks.named("mkdir") {
    doLast {
        mkdir("${projectDir}/src/main/dist/web")
    }
}

afterEvaluate {
    if (!project.extensions.extraProperties.has("frontendDir")) throw Error("project does not have frontendDir property, assign by project.ext[\"frontendDir\"]")
    val frontendDir = file(project.extensions.extraProperties.get("frontendDir") as String)

    tasks.register("buildFrontend") {
        group = "build"
        doLast {
            if (!frontendDir.exists()) throw Error("$frontendDir does not exist")

            exec {
                workingDir(frontendDir)
                commandLine(Frontend.commandLine(listOf("yarn", "install")))
            }

            val command = mutableListOf("yarn", "run", "build")

            val env = project.properties["env"] // use gradlew -Penv=${env} to pass
            if (env != null) command.addAll(listOf("--env", env as String))

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
