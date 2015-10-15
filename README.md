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
                <version>2.0.0</version>
            </dependency>
	    </dependencies>
    </plugin>

Git Usage
---------

In the `pom.xml` the `<distributionManagement>/<site>` needs to be set as
follows:

    <distributionManagement>
        <site>
            <id>gh-pages</id>
            <name>Trajano GitHub Pages</name>
            <url>github:http://trajano.github.io/trajano/</url>
            <!--
            <url>git:ssh://git@github.com/trajano/trajano.git?gh-pages#</url>
            -->
        </site>
    </distributionManagement>

The `<name>` can be any value.

The `<id>` to represent the authentication details in `settings.xml`. At a minimum,
your `settings.xml` file must contain a `<server>` with this `<id>` and a valid
`<username>` element (`git` if using ssh, or your github username if using
http). If you use passwordless-ssh with no password on the key file, then no 
password is necessary.

    <server>
      <id>gh-pages</id>
      <username>git</username>
      <!-- different method for http authentication -->
      <!-- <username>trajano</username> -->
      <!-- supply a password for your ssh keyfile or your http account -->
      <!-- <password>trajano's_pass</password> -->
    </server>    

For [GitHub Pages][GitHubPages] the project URL should be prefixed with `github:`.  If
the host is not in the github.io subdomain, a CNAME lookup will be performed
to determine the [GitHub] user name.

For generic git repositories, the `<url>` must start with `git:` and end
with a `#`.  The branch name must also be specified between the `?` and `#` 
characters.  The `#` at the end is required to prevent additional characters
that will be appended by Maven to be considered as part of the original URL.

Multi-module and project.artifactId
-----------------------------------

Do not use the form in multi-module projects

    <url>github:http://site.trajano.net/${project.artifactId}/</url>

because the sub modules will use the `artifactId` of the current module and
not make it part of the parent which site would be expecting.  This is not
just a limitation of this plugin but is a limitation of the Maven site plugin
itself.  Instead explicity set it on your multi-module project POM e.g. 

    <url>github:http://site.trajano.net/jetng/</url>
    

Protected private keys
----------------------
If you are using a passphrase to protect your private key, then in 
`settings.xml`, then should be a `<server>` definition with the `<id>` 
corresponding to the `<distributionManagement><site><id>` and with a 
`<passphrase>` element but without the `<user>`.  For example:

    <server>
        <id>gh-pages</id>
        <passphrase>privatekeypassphrase</passphrase>
    </server>

If you're not protecting your private key, then no additional changes are 
needed in `settings.xml`

Module and parent references
----------------------------
At the moment if you are referencing the parent project or modules, using the
following in the `site.xml` file.

    <menu ref="parent" inherit="top" />
    <menu ref="modules" inherit="top" />

The problem is a broken implementation of the path logic done by the
[doxia-integation-tools][1] from Maven.  A bug and patch has been provided
by [Archimedes Trajano][Trajano] called [MSITE-709][2] to resolve the issue.  For
those that need the fix immediately, an [alternative doxia-integration-tools][3]
has been made available and can be added as a dependency to the site plugin or
as a build extension.

    <plugin>
        <artifactId>maven-site-plugin</artifactId>
        <dependencies>
            <dependency>
                <groupId>net.trajano.maven.doxia</groupId>
                <artifactId>doxia-integration-tools</artifactId>
                <version>2.0.0</version>
            </dependency>
        </dependencies>
    </plugin>

Non-relative Multi-Module
-------------------------
Projects such as mybatis[4] use a non relative parent. This causes a conflict
when trying to release when both the non-relative parent and submodule uses
`github:` style resulting in NullPointerException similar to the following.

[INFO] Pushing C:\mybatis\ehcache-cache\target\site
[INFO]    >>> to github:ssh://mybatis.github.io/parent/../ehcache-cache

[ERROR]
Failed to execute goal org.apache.maven.plugins:maven-site-plugin:3.4:deploy
(default-cli) on project mybatis-ehcache: Execution default-cli of goal
org.apache.maven.plugins:maven-site-plugin:3.4:deploy failed. NullPointerException

A work around for this issue is to use the `github:` style in the non-relative
parent and to use the generic git style for submodules.

[Trajano]: http://www.trajano.net/
[GitHub]: http://github.com/
[GitHubPages]: https://pages.github.com/
[1]: http://maven.apache.org/shared/maven-doxia-tools/
[2]: http://jira.codehaus.org/browse/MSITE-709
[3]: http://site.trajano.net/maven-doxia-tools/doxia-integration-tools/
[4]: https://github.com/mybatis
