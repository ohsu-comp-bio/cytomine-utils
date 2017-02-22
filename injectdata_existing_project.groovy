/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

//run me with
//groovy -cp 'Cytomine-client-java.jar'  script.groovy
import be.cytomine.client.*;
import be.cytomine.client.collections.*;
import be.cytomine.client.models.*;
import groovy.json.*
import org.json.simple.JSONObject;
import be.cytomine.client.sample.*;

class CYTOMINE {

    static String publickey; //superadmin
    static String privatekey;
    static String cytomineCoreUrl;
    static String cytomineImsUrl;
    static String prefix=""
    static String suffix=""

    static List<String> ignores = []//["DEMO-CLASSIFICATION-CELL","DEMO-EMPTY","DEMO-LANDMARK-ZEBRAFISH-TEST","DEMO-LANDMARK-ZEBRAFISH-TRAINING","DEMO-SEGMENTATION-TISSUE"]
    static LinkedHashMap<String, LinkedHashMap> users = ["jsnow":[:],"clannister":[:],"estark":[:]]

    static Cytomine getCytomine() {
        return new Cytomine(CYTOMINE.cytomineCoreUrl, CYTOMINE.users["jsnow"].publicKey, CYTOMINE.users["jsnow"].privateKey);
    }

    static Cytomine getCytomine2() {
        return new Cytomine(CYTOMINE.cytomineCoreUrl, CYTOMINE.users["clannister"].publicKey, CYTOMINE.users["clannister"].privateKey);
    }

    static Cytomine getCytomineSuperAdmin() {
        return new Cytomine(CYTOMINE.cytomineCoreUrl, CYTOMINE.publickey, CYTOMINE.privatekey);
    }

    static Cytomine getIms() {
        return new Cytomine(CYTOMINE.cytomineImsUrl, CYTOMINE.users["jsnow"].publicKey, CYTOMINE.users["jsnow"].privateKey);
    }
}

println """
   _____      _                  _
  / ____|    | |                (_)
 | |    _   _| |_ ___  _ __ ___  _ _ __   ___
 | |   | | | | __/ _ \\| '_ ` _ \\| | '_ \\ / _ \\
 | |___| |_| | || (_) | | | | | | | | | |  __/
  \\_____\\__, |\\__\\___/|_| |_| |_|_|_| |_|\\___|
 |_   _| __/ (_)         | |       | |     | |
   | |  |___/ _  ___  ___| |_    __| | __ _| |_ __ _
   | | | '_ \\| |/ _ \\/ __| __|  / _` |/ _` | __/ _` |
  _| |_| | | | |  __/ (__| |_  | (_| | (_| | || (_| |
 |_____|_| |_| |\\___|\\___|\\__|  \\__,_|\\__,_|\\__\\__,_|
            _/ |
           |__/

"""

File workingDir = new File(args[0])
CYTOMINE.cytomineCoreUrl = args[1]
CYTOMINE.cytomineImsUrl = args[2]
CYTOMINE.publickey = args[3]
CYTOMINE.privatekey = args[4]
if(args.length>=6) CYTOMINE.prefix = args[5]
if(args.length==7) CYTOMINE.suffix = args[6]

println args
println CYTOMINE.cytomineCoreUrl
println CYTOMINE.cytomineImsUrl
println CYTOMINE.publickey
println CYTOMINE.privatekey
println CYTOMINE.suffix
println CYTOMINE.prefix

//Set keys from jsnow, clannister, estark
CYTOMINE.users["jsnow"].publicKey = CYTOMINE.getCytomineSuperAdmin().getKeysByUsername("jsnow").getStr("publicKey")
CYTOMINE.users["jsnow"].privateKey = CYTOMINE.getCytomineSuperAdmin().getKeysByUsername("jsnow").getStr("privateKey")
CYTOMINE.users["clannister"].publicKey = CYTOMINE.getCytomineSuperAdmin().getKeysByUsername("clannister").getStr("publicKey")
CYTOMINE.users["clannister"].privateKey = CYTOMINE.getCytomineSuperAdmin().getKeysByUsername("clannister").getStr("privateKey")
CYTOMINE.users["estark"].publicKey = CYTOMINE.getCytomineSuperAdmin().getKeysByUsername("estark").getStr("publicKey")
CYTOMINE.users["estark"].privateKey = CYTOMINE.getCytomineSuperAdmin().getKeysByUsername("estark").getStr("privateKey")

println CYTOMINE.users

println "Working directory: ${workingDir.absolutePath}"


//Create injected structure for each project
InjectedData data = new InjectedData()

//project area
Cytomine cytomine = new Cytomine("http://localhost-core", "8cde94f2-9ab4-4053-8c02-b0c8ce75990d", "496e921c-abe4-4c8f-9c8a-878ec171bd2e");
//ProjectCollection projects = cytomine.getProjects();
String[] ignoredImages;
workingDir.listFiles().each { file ->
    if(file.isDirectory() && !file.isHidden() && !CYTOMINE.ignores.contains(file.name)) {
      //removing create project section
      System.out.println("not creating a new project") 
      ProjectCollection projects = cytomine.getProjects(); 
      for(int i=0; i<projects.size();i++){
        Project project = projects.get(i);
        System.out.println(project.get("name"));
        if(project.get("name").equals(file.name)){
          System.out.println("Project exists, will be appending.");
          System.out.println("listing images that are already here:");
          ImageInstanceCollection project_images = cytomine.getImageInstances(project.get("id"));
          System.out.println("This many images are associated with this project:");
          System.out.println(project_images.size());
          ignoredImages = new String[project_images.size()];
          for(int y=0;y<project_images.size();y++){
            ImageInstance image = project_images.get(y);
            System.out.println("these are duplicate images will ignore these");
            System.out.println(image.originalFilename);
            ignoredImages[y] = image.originalFilename;
            System.out.println(ignoredImages[y]); 
          }
        }
      }
      data.projects.add(createProject(file, ignoredImages))
    }
}

println data
println data.projects.collect{it.name}

//Add this structure in Cytomine
injectData(data)

/**
 * Inject data in Cytomine
 */
void injectData(InjectedData data) {

    StorageCollection storages = CYTOMINE.cytomine.getStorages();
    while(storages.size() == 0) {
        storages = CYTOMINE.cytomine.getStorages();
        Thread.sleep(1000);
    }

    //for each project
    data.projects.each { injectedProject ->
        Ontology ontology = null
        //add ontology/terms from ontology.json if ontology not exist
        def jsonOntology = CYTOMINE.cytomine.getOntologies().list.find{it.get("name")==injectedProject.ontology.name}
        if(!jsonOntology) {
            ontology = CYTOMINE.cytomine.addOntology(injectedProject.ontology.name)

            injectedProject.ontology.terms.each {
                CYTOMINE.cytomine.addTerm(it.name,it.color,ontology.getId())
            }

        } else {
            ontology = CYTOMINE.cytomine.getOntology(jsonOntology.id)
        }
        TermCollection terms = CYTOMINE.cytomine.getTermsByOntology(ontology.getId())

        Cytomine cytomine = new Cytomine("http://localhost-core", "8cde94f2-9ab4-4053-8c02-b0c8ce75990d", "496e921c-abe4-4c8f-9c8a-878ec171bd2e");

        System.out.println("Looking for this project");
        System.out.println(injectedProject.name);
        // edit this line with your project id
	Long projid = 2521;
        Project project = cytomine.getProject(projid);
        System.out.println("Found project!");
        System.out.println(project.get("name"));
        
        //for each image: upload image + add annotation from annotation.json
        injectedProject.images.each { injectedImage ->
            println "UPLOAD IMAGE ${new File(injectedImage.filename).name}"
            println "uploadImage(${injectedImage.filename}, ${project.getId()}, ${storages.get(0).id}, ${CYTOMINE.cytomineCoreUrl}, null, false)"
            def json = CYTOMINE.ims.uploadImage(injectedImage.filename, project.getId(), storages.get(0).id, CYTOMINE.cytomineCoreUrl, null, false);

            Long idImageInstance=null

            while(idImageInstance==null) {
                println "Wait for ${new File(injectedImage.filename).name}"
                def value = CYTOMINE.cytomine.getImageInstances(project.getId()).list.find{it.originalFilename==new File(injectedImage.filename).name}
                println value
                idImageInstance = value?.id
                Thread.sleep(5000);
            }

            injectedImage.annotations.each { injectedAnnotation ->

                Cytomine connection = CYTOMINE.cytomine

                //Dirty HACK: if term is "negative", annotation must be add by the user 2
                if(injectedAnnotation.terms.collect{it.name}.contains("Negative")) {
                    connection = CYTOMINE.cytomine2
                }

                Annotation annotation =  connection.addAnnotation(injectedAnnotation.location,idImageInstance)

                println "injectedAnnotation.terms=${injectedAnnotation.terms}"

                injectedAnnotation.terms.each { injectedTerm ->
                    println "injectedTerm=${injectedTerm.name}"
                    println "terms.list=${terms.list}"
                    println terms.list.find{it.name==injectedTerm.name}
                    connection.addAnnotationTerm(annotation.getId(),terms.list.find{it.name==injectedTerm.name}.id)
                }
            }

        }
    }
}



InjectedProject createProject(File directory, String[] ignored) {
    String logPrefix = "*** "

    InjectedProject project = new InjectedProject()
    project.name = CYTOMINE.prefix+directory.getName() + CYTOMINE.suffix

    println logPrefix + project.name

    project.ontology = createOntology(directory)
    // get existing ontology
    //Cytomine cytomine = new Cytomine("http://localhost-core", "8cde94f2-9ab4-4053-8c02-b0c8ce75990d", "496e921c-abe4-4c8f-9c8a-878ec171bd2e");
    
    File imageDir = new File("images",directory);
    println logPrefix + "Check if ${imageDir.absolutePath} exist: ${imageDir.exists()}"

    if(imageDir.exists()) {
        imageDir.listFiles().each { imageFile ->
            System.out.println(imageFile.name);
            boolean contains = false;
            for(int i=0;i<ignored.size();i++){
              if(ignored[i].equals(imageFile.name)){
                contains = true;
              }
            }
            // This is an updated version of this code, which will only register images that have not been previously registered
            // Important if for some reason your upload process hangs or terminates unexpectedly
	    // The other version of injectdata script will create a new project, this appends to new one
            if(contains){
               System.out.println("Image is already registerd... skipping");
            }else{
               System.out.println("Image is not already registered... adding");
               project.images << createImage(imageFile,directory,project)
            }
        }
    }

    project.softwares = createSoftware(directory)

    return project
    //println project;
}

InjectedOntology createOntology(File directory) {
    String logPrefix = "****** "
    File ontologyFile = new File("ontology.json",directory);
    println logPrefix + "Check if ${ontologyFile.absolutePath} exist: ${ontologyFile.exists()}"

    InjectedOntology ontology = new InjectedOntology()

    if(!ontologyFile.exists()) {
        ontology.name = CYTOMINE.prefix+directory.name+CYTOMINE.suffix+"new"
    } else {
        def object = new JsonSlurper().parseText(ontologyFile.text)

        ontology.name = CYTOMINE.prefix+object.name+CYTOMINE.suffix

        List<InjectedTerm> terms = object.children.collect{createTerm(it)}
        ontology.terms = terms
    }
    //println ontology
    return ontology
}

List<InjectedSoftware> createSoftware(File directory) {
    String logPrefix = "****** "
    File softwareFile = new File("software.json",directory);
    println logPrefix + "Check if ${softwareFile.absolutePath} exist: ${softwareFile.exists()}"

    List<InjectedSoftware> softwares = new ArrayList<>()
    if(softwareFile.exists()) {
        def object = new JsonSlurper().parseText(softwareFile.text)
        softwares = object.collection.collect{new InjectedSoftware(name:it.name)}

    }
    return softwares
}

InjectedTerm createTerm(Object term) {
    return new InjectedTerm(name:term.name, color:term.color)
}


InjectedImage createImage(File imageFile, File directory, InjectedProject parent) {
    String logPrefix = "********* "

    InjectedImage image = new InjectedImage(filename: imageFile.absolutePath)

    File annotationFile = new File("annotation.json",directory);
    File imageinstanceFile = new File("imageinstance.json",directory);

    if(annotationFile.exists()) {

        if(!imageinstanceFile.exists()) {
            throw new Exception(imageinstanceFile.absolutePath + " not found!")
        }
        def jsonAnnotations = new JsonSlurper().parseText(annotationFile.text)
        def jsonImageInstances = new JsonSlurper().parseText(imageinstanceFile.text)

        //find the image id
        def jsonImageInstance = jsonImageInstances.collection.find{it.instanceFilename==imageFile.getName()}
        if(!jsonImageInstance) {
            throw new Exception("${imageFile.getName()} not found in ${imageinstanceFile.absolutePath}!")
        }
        Long id = jsonImageInstance.id

        def jsonAnnotationsFromImage = jsonAnnotations.collection.findAll{it.image==id}

        jsonAnnotationsFromImage.each {
            image.annotations << createAnnotations(it,directory,parent)
        }
    }

    return image
}

InjectedAnnotation createAnnotations(Object jsonAnnotation,File directory,InjectedProject parent) {
    String logPrefix = "************ "
    InjectedAnnotation annotation = new InjectedAnnotation()
    annotation.location = jsonAnnotation.location

    println "jsonAnnotation.terms=${jsonAnnotation.term}"
    jsonAnnotation.term.each { idTerm ->
        String termName = new JsonSlurper().parseText(new File("ontology.json",directory).text).children.find{it.id==idTerm}.name


        annotation.terms << parent.ontology.terms.find{it.name==termName}
    }
    return annotation
}








class InjectedData {
    List<InjectedProject> projects = new ArrayList<InjectedProject>();

    String toString() {


        return """
#####################################################################
#####################################################################
#####################################################################
#####################################################################
${projects.join("\n")}
#####################################################################
#####################################################################
#####################################################################
#####################################################################
"""
    }
}

class InjectedProject {
    String name;
    InjectedOntology ontology;
    List<InjectedImage> images = new ArrayList<InjectedImage>();
    List<InjectedSoftware> softwares = new ArrayList<>()

    String toString() {
        return "### Project $name \n$ontology \n$softwares \n"+images.join("\n")
    }
}

class InjectedOntology {
    String name;
    List<InjectedTerm> terms = new ArrayList<InjectedTerm>();

    String toString() {
        return "###### Ontology $name \n${terms.join("\n")}"
    }
}

class InjectedSoftware {
    String name;

    String toString() {
        return "###### Software $name"
    }
}

class InjectedTerm {
    String name;
    String color

    String toString() {
        return "######### Term $name"
    }
}

class InjectedImage {
    String filename;
    List<InjectedAnnotation> annotations = new ArrayList<InjectedAnnotation>();

    String toString() {
        return "###### Image $filename \n${annotations.join("\n")}"
    }
}

class InjectedAnnotation {
    String location;
    List<InjectedTerm> terms = new ArrayList<InjectedTerm>();

    String toString() {
        return "######### Annotation \n${terms.join("\n")}"
    }
}
