Staging Areas Library
====

Overview
----

This library helps organizations maintain common file references in distributed digital work flows, despite file mapping differences across machines and platforms. Software may use the library to create common reference URIs from machine-specific file references. It can also find and create machine-specific references from the common reference URIs. It includes support for automated local discovery and mapping of common staging areas, which are shared between client software and repositories through a JSON file.

###Common Tag URI Approach
One approach to sharing file references is to make a Tag URI. Tag URIs can encode the path to files within commonly understood staging areas:

    tag:myrepo.pendleton.edu,2013:stageOne/project-alpha/photo.tiff

For example, say we stage a file to a network drive on a Windows machine. This gives us a file URI:

    file:/Z:/something/stage-one/project-alpha/photo1.tiff

The shared JSON staging definitions include a staging area that maps to the stage-one folder above:

    "tag:myrepo.pendleton.edu,2013:stageOne":{
      "name":"Shared Staging Area One",
      "mappings":[
        "file:/Z:/something/stage-one/",
        "file:/Volume/Shared/something/stage-one/"
      ],
      "keyFile":".tag_a8cf4064",
      "putPattern":"/${project}/${originalRelPath}",
      "ingestCleanupPolicy":"DELETE_INGESTED_FILES_EMPTY_FOLDERS"
    }

The library uses the mappings and key file information above to discovery the local mapping for this shared staging area.
Then it is possible to rewrite the file reference as a Tag URI:

    tag:myrepo.pendleton.edu,2013:stageOne/project-alpha/photo.tiff
    
This reference is more portable than the local machine file URI. Other software, including the repository, can use this library
to convert the Tag back into a local machine file URI:

    file:/mnt/shared/something/stage-one/project-alpha/photo.tiff 
    
When a project or manifest is involved in a multi-user work flow, the Tag URIs work the same way to maintain file references in
diverse environments.

Requirements
----

This software is packaged as a maven jar, which specifies some minimal dependencies inside the POM file. As of this writing, the only dependencies are SLF4J and Jackson for JSON parsing.

Build
----

After you clone this github project, you can build it with maven:

    # mvn clean install

More Information
----

Several example JSON staging definitions can be found in the src/test/resources folder within the project. Further documentation can be found in the project wiki.
