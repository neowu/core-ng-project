## Overview
core-ng is a webapp framework forked from [Neo's open source project](https://github.com/neowu/core-ng-project).

[![Build Status](https://github.com/neowu/core-ng-project/workflows/build/badge.svg)](https://github.com/neowu/core-ng-project/actions)
[![Code Coverage](https://codecov.io/gh/neowu/core-ng-project/branch/master/graph/badge.svg)](https://codecov.io/gh/neowu/core-ng-project)
[![Code Quality: Java](https://img.shields.io/lgtm/grade/java/g/neowu/core-ng-project.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/neowu/core-ng-project/context:java)
[![Total Alerts](https://img.shields.io/lgtm/alerts/g/neowu/core-ng-project.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/neowu/core-ng-project/alerts)

## Maven repo
```
repositories {
    maven {
        url 'https://neowu.github.io/maven-repo/'
        content {
            includeGroup 'core.framework'
        }
    }
}
```

## Wiki
[https://github.com/neowu/core-ng-project/wiki](https://github.com/neowu/core-ng-project/wiki)

## Change log
please check [CHANGELOG.md](CHANGELOG.md)

## Keep it up-to-date with the upstream repo
```
git remote add upstream https://github.com/neowu/core-ng-project.git
git fetch upstream
git checkout master
git merge upstream/main
```
## Publish to RF Azure packages
Configure the package version in publish.json accordingly and then run command: 
```
./gradlew -PmavenAccessToken=[token] clean publish
```
