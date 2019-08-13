package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.javatuples.Pair;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

public class DropSet {
	// Randomly select 1 axiom from each kernel for the drop set
	public static Set<OWLAxiom> randomSelect(Set<Explanation<OWLAxiom>> kernels) {
		Random rand = new Random(); 
		Set<OWLAxiom> dropSet = new HashSet<OWLAxiom>();
		 
		for(Explanation<OWLAxiom> kernel : kernels) {
			List<OWLAxiom> axioms= new ArrayList<OWLAxiom>(kernel.getAxioms());
			dropSet.add(axioms.get(rand.nextInt(axioms.size())));
		}
		return dropSet;
	} 
	
	public static Set<OWLAxiom> minHittingSetSelect(Set<Explanation<OWLAxiom>> kernels) {
		// Create list of axioms found in kernels
		List<OWLAxiom> kernelAxioms = new ArrayList<OWLAxiom>(); 
		for(Explanation<OWLAxiom> kernel : kernels) {
			for(OWLAxiom axiom : kernel.getAxioms()) {
				kernelAxioms.add(axiom);
			}
		}
				
		// Get all subsets of the kernel axioms
		Set<Set<OWLAxiom>> allSubSets = new HashSet<Set<OWLAxiom>>();
		int n = kernelAxioms.size();
		for(int i = 0; i < (1<<n); i++) {
			Set<OWLAxiom> newSubSet = new HashSet<OWLAxiom>();
					
			for(int j = 0; j < n; j++) {
				if( (i & (1<<j)) > 0) {
					newSubSet.add(kernelAxioms.get(j));	
				}
			}
			allSubSets.add(newSubSet);
		}
				
		// Check if each subset is a hitting set of the kernels
		Set<Set<OWLAxiom>> allHittingSets = new HashSet<Set<OWLAxiom>>();
		for(Set<OWLAxiom> subSet : allSubSets) {
			boolean hitting = true; // True - subset hits all kernels	False - subset doesn't hit all kernels 
			for(Explanation<OWLAxiom> kernel : kernels) {
				if(Collections.disjoint(subSet, kernel.getAxioms())){
					hitting = false;
				}
			}
			// If the subset hits all kernels then add to drop set list
			if(hitting) {
				allHittingSets.add(subSet);
			}
		}
			
		// Get the minimum hitting sets
		int minSize = allHittingSets.iterator().next().size();
		for(Set<OWLAxiom> hittingSet : allHittingSets) {
			if(hittingSet.size() < minSize) {
				minSize = hittingSet.size();
			}
		}
		
		Set<Set<OWLAxiom>> minHittingSets = new HashSet<Set<OWLAxiom>>();
		for(Set<OWLAxiom> hittingSet : allHittingSets) {
			if(hittingSet.size() == minSize) {
				minHittingSets.add(hittingSet);
			}
		}
		
		// User chooses which min hitting set to remove
		if(minHittingSets.size() > 1) {
			return userChooseHittingSet(minHittingSets);
		}
		
		// Remove the only min hitting set
		else {
			return minHittingSets.iterator().next();
		}
			
	}
	

	// Generate all min hitting sets by iterating through all subsets and take the most specific
	public static Set<OWLAxiom> specificityGenericIterative(Set<Explanation<OWLAxiom>> kernels, WeightedAxioms weightedAxioms){
		// Create list of axioms found in kernels
		List<OWLAxiom> kernelAxioms = new ArrayList<OWLAxiom>(); 
		for(Explanation<OWLAxiom> kernel : kernels) {
			for(OWLAxiom axiom : kernel.getAxioms()) {
				kernelAxioms.add(axiom);
			}
		}
		
		// Get all subsets of the kernel axioms
		Set<Set<OWLAxiom>> allSubSets = new HashSet<Set<OWLAxiom>>();
		int n = kernelAxioms.size();
		for(int i = 0; i < (1<<n); i++) {
			Set<OWLAxiom> newSubSet = new HashSet<OWLAxiom>();
			
			for(int j = 0; j < n; j++) {
				if( (i & (1<<j)) > 0) {
					newSubSet.add(kernelAxioms.get(j));	
				}
			}
			allSubSets.add(newSubSet);
		}
		
		// Check if each subset is a hitting set of the kernels
		Set<Set<OWLAxiom>> allHittingSets = new HashSet<Set<OWLAxiom>>();
		for(Set<OWLAxiom> subSet : allSubSets) {
			boolean hitting = true; // True - subset hits all kernels	False - subset doesn't hit all kernels 
			for(Explanation<OWLAxiom> kernel : kernels) {
				if(Collections.disjoint(subSet, kernel.getAxioms())){
					hitting = false;
				}
			}
			// If the subset hits all kernels then add to drop set list
			if(hitting) {
				allHittingSets.add(subSet);
			}
		}
		
		// Get the minimum hitting sets
		int minSize = allHittingSets.iterator().next().size();
		for(Set<OWLAxiom> hittingSet : allHittingSets) {
			if(hittingSet.size() < minSize) {
				minSize = hittingSet.size();
			}
		}
		
		Set<Set<OWLAxiom>> minHittingSets = new HashSet<Set<OWLAxiom>>();
		for(Set<OWLAxiom> hittingSet : allHittingSets) {
			if(hittingSet.size() == minSize) {
				minHittingSets.add(hittingSet);
			}
		}

		// Get the hitting sets with the smallest specificity weighting
		Set<Set<OWLAxiom>> mostSpecificHittingSets = getMostSpecificHittingSets(minHittingSets, weightedAxioms);
		
		// User chooses which min hitting set to remove
		if(mostSpecificHittingSets.size() > 1) {
			return userChooseHittingSet(mostSpecificHittingSets);
		}
				
		// Remove the only min hitting set
		else {
			return mostSpecificHittingSets.iterator().next();
		}
		
		
	}
	
	// Get the hitting sets with the smallest specificity weighting
	private static Set<Set<OWLAxiom>> getMostSpecificHittingSets(Set<Set<OWLAxiom>> minHittingSets, WeightedAxioms weightedAxioms) {
		// If only 1 min hitting set then return the set
		if(minHittingSets.size() == 1) {
			Set<Set<OWLAxiom>> mostSpecificHittingSets = new HashSet<Set<OWLAxiom>>();
			mostSpecificHittingSets.add(minHittingSets.iterator().next());
			return mostSpecificHittingSets;
		}
		
		int min = 1000000000; // Min set weight
		Set<Set<OWLAxiom>> mostSpecificHittingSets = new HashSet<Set<OWLAxiom>>();
		
		int setWeight;
		for(Set<OWLAxiom> hittingSet : minHittingSets) {
			// Find each axiom of set in weightedAxioms and add weight
			setWeight = 0;
			for(OWLAxiom axiom : hittingSet) {
				
				// Subsumptions
				if(axiom.isOfType(AxiomType.SUBCLASS_OF)) {
					for(Pair<OWLSubClassOfAxiom, Integer> subsumption : weightedAxioms.getSubsumptions()) {
						if(subsumption.getValue0().equals(axiom)) {
							setWeight = setWeight + subsumption.getValue1();
						}
					}
				}
				
				// Disjunctions
				if(axiom.isOfType(AxiomType.DISJOINT_CLASSES)) {
					for(Pair<OWLDisjointClassesAxiom, Integer> disjunction : weightedAxioms.getDisjunctions()) {
						if(disjunction.getValue0().equals(axiom)) {
							setWeight = setWeight + disjunction.getValue1();
						}
					}
				}
				
				// Equivalences
				if(axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
					for(Pair<OWLEquivalentClassesAxiom, Integer> equivalence: weightedAxioms.getEquivalences()) {
						if(equivalence.getValue0().equals(axiom)) {
							setWeight = setWeight + equivalence.getValue1();
						}
					}
				}
				
				// Role Subsumptions
				if(axiom.isOfType(AxiomType.SUB_OBJECT_PROPERTY)) {
					for(Pair<OWLSubObjectPropertyOfAxiom, Integer> roleSubsumption : weightedAxioms.getRoleSubsumptions()) {
						if(roleSubsumption.getValue0().equals(axiom)) {
							setWeight = setWeight + roleSubsumption.getValue1();
						}
					}
				}
				
				// Role  Equivalence
				if(axiom.isOfType(AxiomType.EQUIVALENT_OBJECT_PROPERTIES)) {
					for(Pair<OWLEquivalentObjectPropertiesAxiom, Integer> roleEquivalence: weightedAxioms.getRoleEquivalences()) {
						if(roleEquivalence.getValue0().equals(axiom)) {
							setWeight = setWeight + roleEquivalence.getValue1();
						}
					}
				}
			}
			
			// Check if new min weight set
			if(setWeight < min) {
				min = setWeight;
				mostSpecificHittingSets = new HashSet<Set<OWLAxiom>>();
				mostSpecificHittingSets.add(hittingSet);
			}
			// If the same weight add to existing list
			else if(setWeight == min) {
				mostSpecificHittingSets.add(hittingSet);
			}
		}

		return mostSpecificHittingSets;
	}

	// Get user hitting set choice
	private static Set<OWLAxiom> userChooseHittingSet(Set<Set<OWLAxiom>> hittingSets) {
		List<Set<OWLAxiom>> hittingSetList = new ArrayList<Set<OWLAxiom>>(hittingSets);
		
		// Print all min hitting sets 
		System.out.println("Hitting Sets:");
		int index = 1;
		for(Set<OWLAxiom> hittingSet : hittingSetList) {
			System.out.println(index + ") ");
			for(OWLAxiom ax : hittingSet) {
				System.out.println(ax);
			}
			System.out.println("");
			index++;
		}
		
		// Ask user for which set to remove
		Scanner reader = new Scanner(System.in);
		int choice = 0;
		System.out.println("Multiple minimum hitting sets.");
		do {
			System.out.println("\nPlease choose which set to remove: ");
			while(!reader.hasNextInt()) {
				System.out.println("\nPlease choose which set to remove: ");
				reader.next();
			}
			choice = reader.nextInt();		
		} while(choice<=0 ||- choice>hittingSets.size()); // Must be in range
		
		System.out.println("");
		reader.close();

		return hittingSetList.get(choice-1);
	}
	
}
