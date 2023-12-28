include("core-ng-api")
include("core-ng", "core-ng-test")
include("core-ng-search", "core-ng-search-test")
include("core-ng-mongo", "core-ng-mongo-test")

include(
    "ext:log-processor",
    "ext:log-collector",
    "ext:log-exporter",
    "ext:monitor"
)
