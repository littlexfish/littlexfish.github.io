
plugins {
	kotlin("multiplatform") version "1.9.21"
}

group = "org.lf"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

kotlin {
	js(IR) {
		moduleName = "app"
		browser {
			webpackTask {
				mainOutputFileName = "app.js"
				output.libraryTarget = "commonjs2"
				cssSupport {
					enabled = true
				}
			}
			distribution {
				outputDirectory = File("$projectDir/docs/")
			}
		}
		binaries.executable()
	}
	sourceSets {
		val jsMain by getting {
			dependencies {

				implementation(kotlin("stdlib-js"))
				implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.8.0")
				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC")
			}
		}
	}
}

