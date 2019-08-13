package test;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class Contraction {
	public static void kernelContraction(OWLOntology ontology, WeightedAxioms weightedAxioms, OWLAxiom belief, 
										 OWLDataFactory df, OWLOntologyManager ontologyManager, int heuristic) {
		
		// Create an ELK reasoner.
		OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
	
		// Create explanation generator
		ExplanationGeneratorFactory<OWLAxiom> explanationFactory = ExplanationManager.createExplanationGeneratorFactory(reasonerFactory);
		ExplanationGenerator<OWLAxiom> explanationGenerator = explanationFactory.createExplanationGenerator(ontology);
				
		// Find kernels for belief to contract
		Set<Explanation<OWLAxiom>> kernels = getKernels(belief, explanationGenerator);
		
		System.out.println("Belief: \n" + belief + "\n");
		
		// Check if beief is entailed
		if(kernels.isEmpty()) {
			System.out.println("Belief is not entailed by ontology");
			return;
		}
		
		// Print kernels
		printKernals(kernels);
			
		// Find hitting set of kernels
		Set<OWLAxiom> dropSet = new HashSet<OWLAxiom>();
		
		// Specificty
		if(heuristic == 1) {
			dropSet = DropSet.specificityGenericIterative(kernels, weightedAxioms);
		}
		
		// Localization
		else if(heuristic == 2) {
			
		}
		
		// No heuristic
		else {
			dropSet = DropSet.minHittingSetSelect(kernels);
		}
		
		// Print dropset
		printDropSet(dropSet);
		
		// Remove hitting set axioms from ontology
		removeDropSet(dropSet, ontology, ontologyManager);			
		
	}
	
	



	// Get set of kernels of given belief
	private static Set<Explanation<OWLAxiom>> getKernels(OWLAxiom belief, ExplanationGenerator<OWLAxiom> explanationGenerator) {
		Set<Explanation<OWLAxiom>> kernels = explanationGenerator.getExplanations(belief);
		return kernels;
	}

	// Remove drop set axioms from ontology
	private static void removeDropSet(Set<OWLAxiom> dropAxioms, OWLOntology elOntology,
			 						     OWLOntologyManager elOntologyManager) {
		 elOntologyManager.removeAxioms(elOntology, dropAxioms);
	}

	// Print kernels to user
	private static void printKernals(Set<Explanation<OWLAxiom>> kernels) {
		System.out.println("Kernels: ");
		int index = 1;
		for (Explanation<OWLAxiom> kernel : kernels) {
			System.out.println(index + ")");
			for(OWLAxiom a : kernel.getAxioms()) {
				System.out.println(a);
			}
			System.out.println("");
			index++;
		}
		
	}
	
	// Print drop set to user
	private static void printDropSet(Set<OWLAxiom> dropSet) {
		System.out.println("Dropping: ");
		for (OWLAxiom x : dropSet) {
			System.out.println(x);
		}
	}


}
