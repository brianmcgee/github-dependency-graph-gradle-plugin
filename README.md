# GitHub Dependency Graph Gradle Plugin

A Gradle plugin for generating a GitHub dependency graph for a Gradle build, which can be uploaded to the [GitHub Dependency Submission API](https://docs.github.com/en/rest/dependency-graph/dependency-submission).

## Usage
This plugin is designed to be used in a GitHub Actions workflow, an is tightly integrated into the [Gradle Build Action](https://github.com/gradle/gradle-build-action#github-dependency-graph-support).

For other uses, the [core plugin](https://plugins.gradle.org/plugin/org.gradle.github-dependency-graph-gradle-plugin) (`org.gradle.github.GitHubDependencyGraphPlugin`) 
should be applied to the `Gradle` instance via a Gradle init script as follows:

```
import org.gradle.github.GitHubDependencyGraphPlugin
initscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("org.gradle:github-dependency-graph-gradle-plugin:+")
  }
}
apply plugin: GitHubDependencyGraphPlugin
```

This causes 2 separate plugins to be applied, that can be used independently:
- `GitHubDependencyExtractorPlugin` collects all dependencies that are resolved during a build execution and writes these to a file. The output file can be found at `<root>/build/reports/github-depenency-graph-snapshots/<job-correlator>.json`.
- `ForceDependencyResolutionPlugin` creates a `ForceDependencyResolutionPlugin_resolveAllDependencies` task that will attempt to resolve all dependencies for a Gradle build, by simply invoking `dependencies` on all projects.

### Required environment variables

The following environment variables configure the snapshot generated by the `GitHubDependencyExtractorPlugin`. See the [GitHub Dependency Submission API docs](https://docs.github.com/en/rest/dependency-graph/dependency-submission?apiVersion=2022-11-28) for details:
- `GITHUB_DEPENDENCY_GRAPH_JOB_CORRELATOR`: Sets the `job.correlator` value for the dependency submission
- `GITHUB_DEPENDENCY_GRAPH_JOB_ID`: Sets the `job.id` value for the dependency submission
- `GITHUB_DEPENDENCY_GRAPH_REF`: Sets the `ref` value for the commit that generated the dependency graph
- `GITHUB_DEPENDENCY_GRAPH_SHA`: Sets the `sha` value for the commit that generated the dependency graph
- `GITHUB_DEPENDENCY_GRAPH_WORKSPACE`: Sets the root directory of the github repository
- `DEPENDENCY_GRAPH_REPORT_DIR` (optional): Specifies where the dependency graph report will be generated

Each of these values can also be provided via a system property. 
eg: Env var `DEPENDENCY_GRAPH_REPORT_DIR` can be set with `-DDEPENDENCY_GRAPH_REPORT_DIR=...` on the command-line.

### Filtering which Gradle Configurations contribute to the dependency graph

If you do not want to include every dependency configuration in every project in your build, you can limit the
dependency extraction to a subset of these.

To restrict which Gradle subprojects contribute to the report, specify which projects to include via a regular expression.
You can provide this value via the `DEPENDENCY_GRAPH_INCLUDE_PROJECTS` environment variable or system property.

To restrict which Gradle configurations contribute to the report, you can filter configurations by name using a regular expression.
You can provide this value via the `DEPENDENCY_GRAPH_INCLUDE_CONFIGURATIONS` environment variable or system property.

### Controlling the scope of dependencies in the dependency graph

The GitHub dependency graph allows a scope to be assigned to each reported dependency.
The only permissible values for scope are 'runtime' and 'development'.

By default, no scope is assigned to dependencies in the graph. To enable scopes in the generated dependency graph, 
at least one of `DEPENDENCY_GRAPH_RUNTIME_PROJECTS` or `DEPENDENCY_GRAPH_RUNTIME_CONFIGURATIONS` must be configured.

To restrict which Gradle subprojects contribute 'runtime' dependencies to the report, specify which projects to include via a regular expression.
You can provide this value via the `DEPENDENCY_GRAPH_RUNTIME_PROJECTS` environment variable or system property.
For a project not matching this filter, all dependencies will be scoped 'development'.

To restrict which Gradle configurations contribute 'runtime' dependencies to the report, you can filter configurations by name using a regular expression.
You can provide this value via the `DEPENDENCY_GRAPH_RUNTIME_CONFIGURATIONS` environment variable or system property.
Dependencies resolved by a matching configuration will be scoped 'runtime': all other dependencies will be scoped 'development'.

For dependencies that are resolved in multiple projects and/or multiple configurations, only a single 'runtime' scoped resolution
is required for that dependency to be scoped 'runtime'.

### Gradle compatibility

The plugin should be compatible with most versions of Gradle >= 5.2, and has been tested against 
Gradle versions "5.2.1", "5.6.4", "6.0.1", "6.9.4", "7.1.1" and "7.6.3", as well as all patched versions of Gradle 8.x.

The plugin is compatible with running Gradle with the configuration-cache enabled: this support is
limited to Gradle "8.1.0" and later. Earlier Gradle versions will not work with `--configuration-cache`.
Note that no dependency graph will be generated when configuration state is loaded from the configuration-cache.

| Gradle version | Compatible | Compatible with configuration-cache |
| -------------- | ------- | ------------------------ |
| 1.x - 4.x      | :x: | :x: |
| 5.0 - 5.1.1 | :x: | :x: |
| 5.2 - 5.6.4 | ✅ | :x: |
| 6.0 - 6.9.4 | ✅ | :x: |
| 7.0 - 7.0.2 | :x: | :x: |
| 7.1 - 7.6.3 | ✅ | :x: |
| 8.0 - 8.0.2 | ✅ | :x: |
| 8.1+ | ✅ | ✅ |

## Building/Testing

To build and test this plugin, run the following task:
```shell
./gradlew check
```

To self-test this plugin and generate a dependency graph for this repository, run:
```shell
./plugin-self-test-local
```

The generated dependency graph will be submitted to GitHub only if you supply a
[GitHub API token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
via the environment variable `GITHUB_TOKEN`.
