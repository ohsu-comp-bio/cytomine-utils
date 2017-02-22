## Cytomine Utils
# This repo contains scripts relative to cytomine usage for cytominedev.ohsu.edu

# Setting up cytomine for the first time
1. add these to the users `/etc/hosts` file:

```
	137.53.221.42       cytominedev.ohsu.edu
	137.53.221.42       localhost-core
	137.53.221.42       localhost-ims
	137.53.221.42       localhost-ims2
	137.53.221.42       localhost-upload
	137.53.221.42       localhost-retrieval
	137.53.221.42       localhost-iip-base
	137.53.221.42       localhost-iip-ventana
	137.53.221.42       localhost-iip-cyto
	137.53.221.42       localhost-iip-jp2000
```

2. make an external API call to make sure that the images are displayed properly in your browser (recommneded google chrome). Access the following link in your browser: `http://localhost-core/api/userannotation/5956833/mask.png`

3. Cytomine is only fully functional when you are at OHSU given the way the test instance was deployed. 

4. The following variables are important for scripting with cytomine API:

```
cytomine_host="localhost-core"
cytomine_public_key="8cde94f2-9ab4-4053-8c02-b0c8ce75990d"
cytomine_private_key="496e921c-abe4-4c8f-9c8a-878ec171bd2e"
```

# Cytomine Image Import Guide
##### This guide explains how to import images from exacloud into the OHSU instance of Cytomine using the Cytomine java client. In general, this guide assumes user naivety with respect to exacloud, Cytomine, HTCondor, java/groovy, etc.

1. Log in to your exacloud account:

      ```
      local~$ ssh exacloud.ohsu.edu
      ```

2. Make a new directory in your exacloud home directory to store your log/err/out files for exacloud jobs:

      ```
      exacloud~$ mkdir logs
      ```

3. Make a directory with your desired Cytomine project name, then make a directory within the project directory called `images`:

      ```
      exacloud~$ mkdir FANCY_PROJECT
      exacloud~$ cd FANCY_PROJECT
      exacloud~/FANCY_PROJECT$ mkdir images
      ```

4. Move project images to the `images` directory, e.g.:

      ```
      exacloud~$ mv FANCY_IMAGES/*.svs FANCY_PROJECT/images/
      ```

    In the event that the image files you wish to import are remote, you may need to generate local symlinks in the project images directory for each image, e.g.:

      ```
      exacloud~$ cd FANCY_PROJECT/images
      exacloud../images$ ls -1 <path to remote image files, e.g. /home/exacloud/lustre1/YOUR-LAB/images/*.svs> | while read line; do ln -s $line $PWD/$(basename $line); done;
      ```

5. Return to your exacloud home directory and get the ohsu-comp-bio repository containing required Cytomine scripts:

      ```
      exacloud~$ git clone https://github.com/ohsu-computational-biology/cytomine-utils.git
      ```

    The repo should contain the `injectdata.groovy` and `testgroovy.sh` scripts, among others. The `injectdata.groovy` script does the heavy lifting in getting images on to Cytomine. The `testgroovy.sh` script executes `injectdata.groovy` and negotiates with the Cytomine java client to get the files you want where you want them.

    NOTE: The department's GitHub account name is subject to change, i.e. it may  be changed from ohsu-computational-biology to ohsu-comp-bio.

    Before continuing, let's take a look at `testgroovy.sh`:

      ```
      #!/bin/bash

      export PATH=/opt/installed/groovy-2.4.6/bin:$PATH

      groovy -cp 'PATH/TO/CYTOMINE/JAVA/CLIENT' injectdata.groovy PATH/TO/IMAGES http://localhost-core http://localhost-upload 8cde94f2-9ab4-4053-8c02-b0c8ce75990d 496e921c-abe4-4c8f-9c8a-878ec171bd2e
      ```

    You can find the Cytomine java client at https://github.com/cytomine/cytomine-java-client/releases. Make sure to get the release with dependencies, i.e. 'cytomine-java-client-1.0-SNAPSHOT-jar-with-dependencies.jar'.

    The two long strings at the end of the command are the public key and private key, respectively.

6. Configure a job submission script:

    There are only four nodes (exanode-3-[0,1,2,3]) on exacloud that are able to access the Cytomine instance, so you have to make sure that the job you are submitting is going to that node. The following job submission script `submit.exa` tells HTCondor to execute `testgroovy.sh`, which contains instructions for the Cytomine java client:

      ```
      common_dir=/home/users/YOUR-USERNAME/
      executable=/home/users/YOUR-USERNAME/testgroovy.sh
      output=$(common_dir)/logs/$(Cluster).$(Process).out
      error=$(common_dir)/logs/$(Cluster).$(Process).err
      log=$(common_dir)/logs/$(Cluster).$(Process).log
      requirements = (machine == "exanode-3-1.local")
      request_cpus = 1
      request_memory = 8 GB
      getenv = True
      notify_user = YOUR-USERNAME@ohsu.edu
      notification = Error
      queue 1
      ```

    Note that `requirements = (machine == "exanode-3-1.local")` specifies that the job be submitted to one of the appropriate nodes.

7. Submit your job.

      ```
      exacloud~$ condor_submit submit.exa
      ```

8. Check your the progress of your job.
One easy way is by reading the tailing contents of its stdout file (which should appear in your logs directory):

      ```
      exacloud~/logs$ tail -f YOUR-JOB.out
      ```

    You should see information about each image as it is uploaded. This is also a good sanity check to make sure that the job isn't stalled out.

    You can check all jobs submitted:

      ```
      exacloud~$ condor_q YOUR-USERNAME
      ```

#### Did you job fail or not produce the desired result? Check the log/err/out files to inform your de-bugging strategy.

#### If any or all of the above fails, consult https://github.com/cytomine and http://exainfo.ohsu.edu/projects/new-user-information/wiki.

## Happy importing!

## Troubleshooting Steps
If for any reason you experience an image upload process that hangs in the middle of import, or is taking forever to upload images here are some sugggestions:

1. If it is taking a long to upload images and you are not uploading tiffs, cytomine will be converting the files to tiffs in the background. If you convert these to tiffs manually then resume the upload process you will see a drastic speed up.

2. If you have a failure, a stall, or a hang while uploading from injectdata.groovy, you will need to resume the process with the `injectdata_existing_project.groovy` script. The only thing that is required is to update line 175 `Long projid = 2521;` with your project id. Restart the import process with this .groovy file in place of the injectdata.groovy file.

If you are still having trouble, you can log onto the VM where cytomine is deployed (cytominedev.ohsu.edu).

groovy2.4.6 on here is: /usr/local/groovy-2.4.6/bin

cytomine source is here: /usr/local/src/Cytomine_src

cytomine runs on docker, run the following commands to see the dockers running to serve cytomine

```
sudo su - 
docker ps -a
```
You can use `docker exec` to log into these to debug and read what the servers are saying. If you are uncomforatble with this please reach out to acc.ohsu.edu.


