import com.github.spotbugs.snom.Confidence

plugins {
    checkstyle apply false
    pmd apply false
    id("com.github.spotbugs") apply false
    jacoco apply false
    id("jacoco-report-aggregation") apply false
}

apply(plugin = "checkstyle")
apply(plugin = "pmd")
apply(plugin = "com.github.spotbugs")
apply(plugin = "jacoco")
apply(plugin = "jacoco-report-aggregation")

checkstyle {
    dependencies {
        checkstyle("com.puppycrawl.tools:checkstyle:11.1.0")
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
    toolVersion = "7.17.0"
    isConsoleOutput = true

    tasks.withType<Pmd> {
        group = "verification"
    }
}

spotbugs {
    dependencies {
        spotbugsPlugins("com.mebigfatguy.sb-contrib:sb-contrib:7.6.12")
    }

    toolVersion = "4.9.6"
    reportLevel = Confidence.LOW
    extraArgs = listOf("-longBugCodes")
    includeFilter = rootProject.file("buildSrc/src/main/check/spotbugs.xml")
}

jacoco {
    toolVersion = "0.8.13"

    tasks.named<JacocoReport>("testCodeCoverageReport") {
        reports {
            xml.required = true
            xml.outputLocation = layout.buildDirectory.file("reports/jacoco/report.xml").get()
            html.required = true
            csv.required = false
        }
    }
}
