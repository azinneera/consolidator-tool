# Consolidate-package bal tool

Ballerina inherently supports microservices-style deployments, which are well-suited for microservice orchestration platforms like Kubernetes.
However, many enterprise users deploy on VMs or Docker, where managing each service as a separate process increases complexity and resource overhead. 
This tool aims to provide a solution tailored to such scenarios by enabling consolidated deployments.

## Usage

### Create or update a consolidator package

Create a new package and add the `consolidate-packages` tool entry as shown in the below example into the `Ballerina.toml` with the services required to consolidate. 
You can add/remove services as needed by updating the values provided for the `options.services` array. The tool will be automatically pulled during
the package build.

```toml
[package]
org = "myorg"
name = "consolidator"
version = "0.1.0"

[[tool.consolidate-packages]]
id = "consolidateSvc"
options.services = ["myorg/svc1", "myorg/svc2"]
```

#### Using the CLI tool
Alternatively, the `consolidate-packages` CLI tool can be installed to create and modify the consolidator package. This
is typically useful in CI/CD pipelines.

An example is shown below.

##### Installation

Execute the command below to pull the tool.

```bash
bal tool pull consolidate-packages
```

##### Creating a new consolidator package
```
$ bal consolidate-packages new --services myorg/svc1,myorg/svc2 [--name <package-name>]
```

##### Adding new services to an existing package
```
$ bal consolidate-packages add --services myorg/svc3,myorg/svc4
```

##### Removing services from an existing package
```
$ bal consolidate-packages remove --services myorg/svc2,myorg/svc3
```

## Building from the Source

### Setting Up the Prerequisites

1. OpenJDK 21 ([Adopt OpenJDK](https://adoptopenjdk.net/) or any other OpenJDK distribution)

   >**Info:** You can also use [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html). Set the JAVA_HOME environment variable to the pathname of the directory into which you installed JDK.

2. Export GitHub Personal access token with read package permissions as follows,
   ```bash
   export packageUser=<Username>
   export packagePAT=<Personal access token>
   ```

### Building the Source

Execute the commands below to build from the source.

1. To build the library:

        ./gradlew clean build

2. To run tests:

        ./gradlew clean test

3. To build the module without the checks (including tests):

        ./gradlew clean build -x check

4. To publish to maven local:

        ./gradlew clean build publishToMavenLocal

5. Publish the generated artifacts to the Ballerina local repository:

        ./gradlew clean build -PpublishToLocalCentral=true

6. Publish the generated artifacts to the Ballerina central repository:

        ./gradlew clean build -PpublishToCentral=true

## Contributing to Ballerina

As an open-source project, Ballerina welcomes contributions from the community.

You can also check for [open issues](https://github.com/ballerina-platform/wsdl-tools/issues) that
interest you. We look forward to receiving your contributions.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of Conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful Links

* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
* View the [Ballerina performance test results](https://github.com/ballerina-platform/ballerina-lang/blob/master/performance/benchmarks/summary.md).
