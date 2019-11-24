import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    `maven-publish`
}

version = "0.1.0-dev"

android {
    compileSdkVersion(property("targetSdkVersion") as Int)
    defaultConfig {
        minSdkVersion(property("minSdkVersion") as Int)
        targetSdkVersion(property("targetSdkVersion") as Int)
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}

kotlin {
    js {
        val main by compilations.getting {
            kotlinOptions {
                moduleKind = "commonjs"
            }
        }
    }
    android {
        publishLibraryVariants("release", "debug")
    }
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":firebase-app"))
                implementation(project(":firebase-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.14.0")
            }
        }
        val androidMain by getting {
            dependencies {
                api("com.google.firebase:firebase-firestore:19.0.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
            }
            kotlin.srcDir("src/androidMain/kotlin")
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.14.0")
            }
        }
    }
}

tasks {
    val copyPackageJson by registering(Copy::class) {
        from(file("package.json"))
        into(file("$buildDir/node_module"))
    }

    val copyJS by registering {
        doLast {
            val from = File("$buildDir/classes/kotlin/js/main/${project.name}.js")
            val into = File("$buildDir/node_module/${project.name}.js")
            into.createNewFile()
            into.writeText(from.readText()
                .replace("require('firebase-", "require('@teamhubapp/firebase-")
            )
        }
    }


    val copySourceMap by registering(Copy::class) {
        from(file("$buildDir/classes/kotlin/js/main/${project.name}.js.map"))
        into(file("$buildDir/node_module"))
    }

    val publishToNpm by registering(Exec::class) {
        doFirst {
            mkdir("$buildDir/node_module")
        }

        dependsOn(copyPackageJson, copyJS, copySourceMap)
        workingDir("$buildDir/node_module")
        if(Os.isFamily(Os.FAMILY_WINDOWS)) {
            commandLine("cmd", "/c", "npm publish --registry https://npm.pkg.github.com/")
        } else {
            commandLine("npm", "publish", "--registry https://npm.pkg.github.com/")
        }
    }
}