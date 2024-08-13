rootProject.name = "koh"
include("core", "datahub", "deliver")
project(":core").projectDir = file("core")
project(":datahub").projectDir = file("datahub")
project(":deliver").projectDir = file("deliver")
