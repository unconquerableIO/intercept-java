plugins {
    base
    id("pl.allegro.tech.build.axion-release") version "1.18.2"
}

scmVersion {
    tag {
        prefix.set("v")
        initialVersion(pl.allegro.tech.build.axion.release.domain.hooks.HooksConfig.VERSION_FROM_TAG, "0.0.2")
    }
    nextVersion {
        suffix.set("SNAPSHOT")
        separator.set("-")
    }
}

allprojects {
    group   = property("projectGroup").toString()
    version = rootProject.scmVersion.version
}