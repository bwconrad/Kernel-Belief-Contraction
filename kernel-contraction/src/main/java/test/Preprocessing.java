package test;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;

public class Preprocessing {

	// Remove EL violations from input ontology and return new EL ontology
	public static OWLOntology reduceToElOntology(OWLOntology originalOntology, OWLOntologyManager ontologyManager) throws OWLOntologyCreationException {
		// Check if ontology is EL
		OWL2ELProfile  profile = new OWL2ELProfile();
		OWLProfileReport report = profile.checkOntology(originalOntology);
				
		// Get list of EL violating axioms
		Set<OWLAxiom> nonElAxioms = new HashSet<OWLAxiom>();
		List<OWLProfileViolation> violations = report.getViolations();
					
		for(OWLProfileViolation v : violations) {
			nonElAxioms.add(v.getAxiom()); 
		}
				
		// Create EL ontology with all non violating axioms
		OWLOntology elOntology = ontologyManager.createOntology();
					
		Set<OWLAxiom> originalAxioms = originalOntology.getAxioms(); // All axioms in the original ontology
		
		for(OWLAxiom axiom : originalAxioms) {
			// Put the EL axioms into the new ontology
			if(!nonElAxioms.contains(axiom)) {
				ontologyManager.addAxiom(elOntology, axiom);
			}
		}
		return elOntology;
	}
	
}
