snagjar-maven-plugin
====================

Maven plugin written in Scala to allow installation and deployment of arbitrary jars into local and remote m2 repos based on embedded maven metadata.

Installation
============

Check out source to local working copy and install:

    git clone https://github.com/adamcin/snagjar-maven-plugin.git snagjar-maven-plugin
    cd snagjar-maven-plugin
    maven install

To enable shorthand execution of the plugin from the command-line, you must add the groupId to your pluginGroups element in your maven settings.xml file:

    <pluginGroups>
      <pluginGroup>net.adamcin</pluginGroup>
    </pluginGroups>
    
Usage
=====

The snagjar-maven-plugin provides 5 goals:

- _to-log_: Prints the details of snagged artifacts to the maven log
- _to-deps_: Generates a maven pom file containing a sorted list of unique captured artifacts as dependencies in a dependencyManagement element
- _to-local_: Installs snagged artifacts to the local maven repository
- _to-remote_: Deploys snagged artifacts to a remote maven repository
- _help_: Prints a list of the plugin's goals and available parameters for each, though without javadoc-based documentation because the Mojos are written in Scala

To install all the jars in the current directory into the local maven repository:

    mvn snagjar:to-local

To recursively scan the current directory for artifacts and generate a dependencyManagement section with all dependencies defined as having _provided_ scope:

    mvn snagjar:to-deps -Drecursive=true -Dscope=provided
    
To identify a single jar using the _to-log_ mojo:

    mvn snagjar:to-log -DsnagFile=someBundle.jar
    
To recursively scan a CQ installation directory for bundles and deploy them to a remote repo in order to populate it with project dependencies:

    mvn snagjar:to-remote -DsnagFile=crx-quickstart -Drecursive=true -DrepositoryId=thirdparty -Durl=http://example.corp.com/repository/thirdparty/content
    
To recursively scan a CQ installation directory and generate a dependencyManagement section that only includes proprietary libraries:

    mvn snagjar:to-deps -Dfilter=com.* -Drecursive=true -FsnagFile=crx-quickstart -DdepsFile=C:/workspace/cq55platform/pom.xml
    
// Test    
