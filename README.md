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
                <version>${project.version}</version>
            </dependency>
	    </dependencies>
    </plugin>

Usage
-----

In the `pom.xml` the `<distributionManagement>/<site>` needs to be set as
follows:

    <distributionManagement>
        <site>
            <id>gh-pages</id>
            <name>Trajano GitHub Pages</name>
            <url>git:ssh://git@github.com/trajano/trajano.git?gh-pages#</url>
        </site>
    </distributionManagement>

The `<id>` to represent the authentication details in `settings.xml`.

The `<name>` can be any value.

The `<url>` must start with `git:` and end with a `#`.  The branch name must
also be specified between the `?` and `#` characters.  The `#` at the end is
required to prevent additional characters that will be appended by Maven to
be considered as part of the original URL.

Protected private keys
----------------------
If you are using a passphrase to protect your private key, then in 
`settings.xml`, then should be a `<server>` definition with the `<id>` 
corresponding to the `<distributionManagement><site><id>` without the 
`<user>` for example:

    <server>
        <id>gh-pages</id>
        <password>privatekeypassword</password>
    </server>

If you're not protecting your private key, then no additional changes are 
needed in `settings.xml`

Parent project reference
------------------------
At the moment if you are referencing the parent project, the URL does not
get rendered correctly and there is no way around it at the moment.  This
is a fundamental flaw in how the Maven site is built by Maven core plugins,
but determining which component (e.g. Doxia, maven-site-plugin, plexus)
isn't trivial.
