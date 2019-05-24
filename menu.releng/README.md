# Release engineering

When developing Eclipse plug-in you usually have 2 tools to process a build:

- PDE (Eclipse Plug-in development environment)
- Eclipse Tycho Maven plugin. 

PDE is quite hard to use in automated builds and is usually invoked in manual builds within Eclipse IDE. 
Tycho, on the other hand, was designed for automated builds with Maven.
Che4 supports both tools, i.e. you can build everything in Eclipse IDE or invoke Maven build command, that calls Tycho plugin to do the job.

## Development process model

The development process is based on the notorious [Git Flow model](http://nvie.com/posts/a-successful-git-branching-model/). We always use
`SNAPSHOT` versions except in `master` branch that holds the source code for release only.

### How release is prepared?

Only Travis CI is responsible for preparing and publishing releases to `master` branch.

1. The process is triggered when git's `tag` is pushed to github repository in `develop` branch.
2. Travis executes under the hood `mvn -P prepare-release`. The 2 main steps are conducted:
	- change version from SNAPSHOT to releases (e.g. from `0.0.1-SNAPHSOT` to `0.0.1`) in `pom.xml` files.
	- change OSGi version of artifacts (e.g. in MANIFEST.MF, feature.xml) to release-like version (e.g. from `0.0.1.qualifier`
to `0.0.1`).
4. Process the build (`mvn`)
5. Process deploy (`mvn deploy:deploy`) to [bintray.com](https://bintray.com/che4/maven). Maven-deploy-plugin is assigned phase `none`, so runing `mvn deploy` won't actually deploy.
6. run `git stash` command to save changes in local git
7. switch to `master` branch &ndash; `git checkout master`
8. get files from `stash` &ndash; `git stash apply`
9. accept all changes &ndash; `git checkout --theirs -- .`
10. `git add .`
11. `git commit -m 'New release'`
12. get release version from maven-generated properties (say MyNewVersion) &ndash; `git tag 'MyNewVersion'`
13. push to github: `git push -u origin master`


## pom.xml differences in master vs develop branches





```java

public static void hello(){
	System.out.println("Helloaaaa!");
}

```


	<!-- TEST is configured according to this article http://mukis.de/pages/simple-junit-tests-with-tycho-and-surefire/ -->
	<!-- Consider to include POM-dependencies in Target Platform Definition - http://andriusvelykis.github.io/pde-target-maven-plugin/ -->