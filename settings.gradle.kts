dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "nix-dependency-graph-gradle-plugin"
include("plugin")
include("plugin-test")
