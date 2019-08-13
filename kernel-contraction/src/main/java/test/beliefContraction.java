package test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class beliefContraction {
	public static void main(String[] args) throws OWLOntologyStorageException,
	OWLOntologyCreationException, ParserException {
		LogManager.getLogger("org.semanticweb.elk").setLevel(Level.ERROR); // Disable reasoner logging messages
		
		// Check input count
		Parsing.checkInputCount(args);
		
		// Get heuristic type
		int heuristic = Parsing.parseHeuristic(args);

		// Create ontology managers
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager(); // Manager for inputed ontology
		OWLOntologyManager elOntologyManager = OWLManager.createOWLOntologyManager(); // Manager for EL reduced ontology
		
		// Create datafactory
		OWLDataFactory df = elOntologyManager.getOWLDataFactory();
		
		// Load ontology
		OWLOntology ontology = Parsing.loadOntology(ontologyManager, args);

		// Reduce ontology to EL 
		OWLOntology elOntology = Preprocessing.reduceToElOntology(ontology, elOntologyManager);
		
		// Parse belief
		OWLAxiom belief = Parsing.parseBelief(args, elOntology, elOntologyManager, df);
		
		// Apply contraction using specified heuristic
		if(heuristic == 1) {
			specificity(elOntology, elOntologyManager, df, belief, heuristic);
		}
		else if(heuristic == 2) {
			localization();
		}
		
		else {
			noHeuristic(elOntology, belief, df, elOntologyManager, heuristic);
		}
		
		
		// Save new ontology
		try {
			elOntologyManager.saveOntology(elOntology, new FileOutputStream(args[1]));
		} catch (FileNotFoundException e) {
			System.out.println("Error while saving. Output path does not exist.");
		}
		

	}
	
	
	// Apply contract of belief on ontology using no heuristic
	private static void noHeuristic(OWLOntology elOntology, OWLAxiom belief, OWLDataFactory df, OWLOntologyManager elOntologyManager, int heuristic) {
		// Contract belief from the ontology
		Contraction.kernelContraction(elOntology, null, belief, df, elOntologyManager, heuristic);
		
	}

	// TODO
	// Apply contract of belief on ontology using localization heuristic
	private static void localization() {
		System.out.println("TODO");
		
	}

	// Apply contract of belief on ontology using specificity heuristic
	public static void specificity(OWLOntology elOntology, OWLOntologyManager elOntologyManager, OWLDataFactory df, OWLAxiom belief, int heuristic) throws OWLOntologyCreationException {
		// Assign specificity weightings
		WeightedAxioms weightedAxioms = Specificity.getWeightedAxioms(elOntology, elOntologyManager, df);
		
		// Contract belief from the ontology
		Contraction.kernelContraction(elOntology, weightedAxioms, belief, df, elOntologyManager, heuristic);
	}
}

