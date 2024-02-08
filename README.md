## Overview
core-ng is a webapp framework, it's designed to support our own projects.

[![Build](https://github.com/neowu/core-ng-project/actions/workflows/build.yml/badge.svg)](https://github.com/neowu/core-ng-project/actions/workflows/build.yml)
[![CodeQL](https://github.com/neowu/core-ng-project/actions/workflows/codeql.yml/badge.svg)](https://github.com/neowu/core-ng-project/actions/workflows/codeql.yml)
[![Code Coverage](https://codecov.io/gh/neowu/core-ng-project/branch/master/graph/badge.svg)](https://codecov.io/gh/neowu/core-ng-project)

## Maven repo
```
repositories {
    maven {
        url = uri("https://neowu.github.io/maven-repo/")
        content {
            includeGroupByRegex("core\\.framework.*")
        }
    }
    maven {
        url = uri("https://maven.codelibs.org/")
        content {
            includeGroup("org.codelibs.elasticsearch.module")
        }
    }
}
```

## Wiki
[https://github.com/neowu/core-ng-project/wiki](https://github.com/neowu/core-ng-project/wiki)

## Change log
please check [CHANGELOG.md](CHANGELOG.md)

