rootProject.name = "koh-vps-provider"
include("datahub", "vps-provider")
project(":datahub").projectDir = file("datahub")
project(":vps-provider").projectDir = file("vps-provider")
