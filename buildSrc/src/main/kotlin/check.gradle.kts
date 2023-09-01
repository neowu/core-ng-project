import com.github.spotbugs.snom.Confidence

plugins {
    checkstyle apply false
    pmd apply false
    jacoco apply false
    id("com.github.spotbugs") apply false
    id("jacoco-report-aggregation") apply false
}

subprojects {
    if (!plugins.hasPlugin("java")) return@subprojects      // only apply to projects have java plugin

    apply(plugin = "checkstyle")
    apply(plugin = "pmd")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "jacoco")
    apply(plugin = "jacoco-report-aggregation")

    checkstyle {
        dependencies {
            checkstyle("com.puppycrawl.tools:checkstyle:10.12.2")
            checkstyle("com.github.sevntu-checkstyle:sevntu-checks:1.44.1")
        }

        configFile = rootProject.file("buildSrc/src/main/check/checkstyle.xml")
        configProperties["configDir"] = configFile.parentFile

        tasks.named<Checkstyle>("checkstyleMain") {
            group = "verification"
            source = fileTree(projectDir) {
                include("conf/**/*.properties")
                include("src/main/java/**/*.java")
                include("src/main/**/*.properties")
            }
        }

        tasks.named<Checkstyle>("checkstyleTest").configure {
            group = "verification"
            source = fileTree(projectDir) {
                include("src/test/java/**/*.java")       // not include java files in resources
                include("src/test/**/*.properties")
            }
        }
    }

    pmd {
        ruleSets = listOf()
        ruleSetFiles = rootProject.files("buildSrc/src/main/check/pmd.xml")
        toolVersion = "6.55.0"
        isConsoleOutput = true

        tasks.withType<Pmd> {
            group = "verification"
        }
    }

    spotbugs {
        dependencies {
            spotbugsPlugins("com.mebigfatguy.sb-contrib:sb-contrib:7.6.0")
        }

        toolVersion.set("4.7.3")
        reportLevel.set(Confidence.LOW)
        extraArgs.set(listOf("-longBugCodes"))
        includeFilter.set(rootProject.file("buildSrc/src/main/check/spotbugs.xml"))
    }

    jacoco {
        toolVersion = "0.8.10"

        tasks.named<JacocoReport>("testCodeCoverageReport") {
            reports {
                xml.required = true
                xml.outputLocation = rootProject.file("${layout.buildDirectory}/reports/jacoco/report.xml")
                html.required = true
                csv.required = false
            }
        }
    }
}
