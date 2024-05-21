package ie.bmcgee.nix

import org.gradle.api.Plugin
import org.gradle.api.invocation.Gradle
import org.gradle.dependencygraph.AbstractDependencyExtractorPlugin
import org.gradle.forceresolve.ForceDependencyResolutionPlugin

@Suppress("unused")
class NixDependencyGraphPlugin : Plugin<Gradle> {
    override fun apply(gradle: Gradle) {
        // Only apply the dependency extractor to the root build
        if (gradle.parent == null) {
            gradle.pluginManager.apply(NixDependencyExtractorPlugin::class.java)
        }

        // Apply the dependency resolver to each build
        gradle.pluginManager.apply(ForceDependencyResolutionPlugin::class.java)
    }

    class NixDependencyExtractorPlugin : AbstractDependencyExtractorPlugin() {
        override fun getRendererClassName(): String {
            return NixDependencyGraphRenderer::class.java.name
        }
    }
}