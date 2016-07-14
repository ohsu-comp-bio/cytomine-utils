#!/bin/bash

export PATH=/usr/local/groovy/bin:$PATH

groovy -cp '/usr/local/src/Cytomine_src/cytomine-java-client-1.0-SNAPSHOT-jar-with-dependencies.jar' injectdata.groovy ./ http://localhost-core http://localhost-upload 8cde94f2-9ab4-4053-8c02-b0c8ce75990d 496e921c-abe4-4c8f-9c8a-878ec171bd2e _PROJECT
