Git Wagon Provider
==================

This is a Wagon Provider for Git that works with GitHub Pages and works
with multi-module projects.

Registering the plugin
----------------------
The plugin can be registered as a dependency in the `maven-site-plugin` or
as an `extension` for the project.  It is recommended that the dependency
approach is used to prevent polluting the maven namespace.

    <plugin>
        <artifactId>maven-site-plugin</artifactId>
	<version>3.3</version>
	<dependencies>
	    <dependency>
	        <groupId>net.trajano.wagon</groupId>
	        <artifactId>wagon-git</artifactId>
	        <version>1.0.0-SNAPSHOT</version>
	    </dependency>
	<dependencies>
    </plugin>

Usage
-----

In the `pom.xml` the `<distributionManagement>/<site>` needs to be set as
follows:

    <distributionManagement>
        <site>
            <id>gh-pages</id>
	    <name>Trajano Maven Site Deployment</name>
	    <url>git:ssh://git@github.com/trajano/trajano.git?gh-pages#</url>
        </site>
    </distributionManagement>

The `<id>` should correspond to any authentication settings in `settings.xml`,
for example:

    <server>
        <id>gh-pages</id>
        <username>git</username>
    </server>

The `<name>` can be any value.

The `<url>` must start with `git:` and end with a `#`.  The branch name must
also be specified between the `?` and `#` characters.  The `#` at the end is
required to prevent additional characters that will be appended by Maven to
be considered as part of the original URL.

