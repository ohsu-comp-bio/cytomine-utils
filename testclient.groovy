
import be.cytomine.client.*;
import be.cytomine.client.collections.*;
import be.cytomine.client.models.*;
import groovy.json.*
import org.json.simple.JSONObject;
import be.cytomine.client.sample.*;

Cytomine cytomine = new Cytomine("http://localhost-core", "8cde94f2-9ab4-4053-8c02-b0c8ce75990d", "496e921c-abe4-4c8f-9c8a-878ec171bd2e");

System.out.println("Hello " + cytomine.getCurrentUser().get("username"));
System.out.println("******* You have access to these projects: *******");
ProjectCollection projects = cytomine.getProjects();
// delete projects that aren't T1
for(int i=0;i<projects.size();i++) {
    Project project = projects.get(i);
    System.out.println(project.get("name"));
    System.out.println(project.get("id"));
    System.out.println(project.get("ontology"));
    //OntologyCollection ontos = cytomine.getOntologiesByProject(project.get("id"));
    //System.out.println("made it here");
    //System.out.println(ontos.size());
    //for(int y=0;y<ontos.size();y++){
    //  System.out.println(y);
    //  Ontology o = ontos.get(y);
    //  System.out.println(project.get("name"));
    //  System.out.println(o.get("name"));
    //}
    //if(!(project.get("name").equals("T1"))){
    //System.out.println(project.get("name"));
    //System.out.println(project.get("id"));
    //  cytomine.deleteProject(project.get("id")); 
    //}
}

// delete ontologies
//OntologyCollection ontologies = cytomine.getOntologies();
//for(int i=0; i<ontologies.size();i++){
  //Ontology ontology = ontologies.get(i);
  //if(!(ontology.get("name").equals("T1"))){
 // System.out.println(ontology.get("name"));
  //ProjectCollection projontos = cytomine.getProjectsByOntology(ontology.get("id"));
  //if(projontos.size() == 0){
   // System.out.println("trying to delete onto");
   // cytomine.deleteOntology(ontology.get("id"));
  //}
  //for(int y=0; y<projontos.size(); y++){
    //System.out.println("in project area");
    //Project proj = projontos.get(y);
    //System.out.println(proj.get("name"));
  //}
    //cytomine.deleteOntology(ontology.get("id"));
  //}
//}
