# Prepare release

## Format source code

```
mvn formatter:format
```

## Update license headers

```
mvn license:format
```

## Check for updated dependency versions

```
mvn versions:display-dependency-updates
```

## Check for updated plugin versions

```
mvn versions:display-plugin-updates
```

# Release process 

1. Check `pom.xml` for the proper `<version />` tag
1. Check `pom.xml` `<Specification-Version />` entries
1. Update `src/changes/changes.xml` timestamp of the release version
1. Update `README.md` version for the Maven examples

Then execute

```
  $ mvn release:prepare && mvn release:perform
```

which will tag, build, test and upload the artifacts to Sonatype's OSS staging area & closes the staging repository.
Maven Central synchronization usually takes ~15 minutes.

Finally create a new section in `src/changes/changes.xml`.
