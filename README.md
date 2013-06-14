Overview

This library helps organizations maintain common file references in distributed digital work flows, despite file mapping differences across machines and platforms. Software may use the library to create common reference URIs from machine-specific file references. It can also find and create machine-specific references from the common reference URIs. It includes support for automated local discovery and mapping of common staging areas, which are shared between client software and repositories through a JSON file.

Requirements

This software is packaged as a maven jar, which specifies some minimal dependencies inside the POM file. As of this writing, the only dependencies are SLF4J and Jackson for JSON parsing.

Build

After you clone this github project, you can build it with maven:
  mvn clean install

More Information
Several example JSON staging definitions can be found in the src/test/resources folder within the project. Further documentation can be found in the project wiki.
