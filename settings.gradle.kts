pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    versionCatalogs {
        /*libs {
            from(files("gradle/libs.versions.toml"))
        }*/
        create("testLibs") {
            from(files("gradle/libs-test.versions.toml"))
        }
    }
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "View System"
include(":app")
include(":external:calendarview")
 