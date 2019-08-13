package test;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

public class WeightedAxioms {
	private List<Pair<OWLSubClassOfAxiom, Integer>> weightedSubsumptions;
	private List<Pair<OWLDisjointClassesAxiom, Integer>> weightedDisjunctions;
	private List<Pair<OWLEquivalentClassesAxiom, Integer>> weightedEquivalences;
	
	private List<Pair<OWLSubObjectPropertyOfAxiom, Integer>> weightedRoleSubsumptions;
	private List<Pair<OWLEquivalentObjectPropertiesAxiom, Integer>> weightedRoleEquivalences;
	
	
	public WeightedAxioms(List<OWLSubClassOfAxiom> subsumptions, 
						  List<OWLDisjointClassesAxiom> disjunctions, 
						  List<OWLEquivalentClassesAxiom> equivalences,
						  List<OWLSubObjectPropertyOfAxiom> roleSubsumptions,
						  List<OWLEquivalentObjectPropertiesAxiom> roleEquivalences)
		{
		weightedSubsumptions = new ArrayList<Pair<OWLSubClassOfAxiom, Integer>>();
		for(int i = 0; i < subsumptions.size(); i++) {
			 weightedSubsumptions.add(new Pair<OWLSubClassOfAxiom, Integer>(subsumptions.get(i), -1));			
		}
		
		weightedDisjunctions= new ArrayList<Pair<OWLDisjointClassesAxiom, Integer>>();
		for(int i = 0; i < disjunctions.size(); i++) {
			 weightedDisjunctions.add(new Pair<OWLDisjointClassesAxiom, Integer>(disjunctions.get(i), -1));			
		}
		
		weightedEquivalences = new ArrayList<Pair<OWLEquivalentClassesAxiom, Integer>>();
		for(int i = 0; i < equivalences.size(); i++) {
			 weightedEquivalences.add(new Pair<OWLEquivalentClassesAxiom, Integer>(equivalences.get(i), -1));			
		}
		
		weightedRoleSubsumptions= new ArrayList<Pair<OWLSubObjectPropertyOfAxiom, Integer>>();
		for(int i = 0; i < roleSubsumptions.size(); i++) {
			 weightedRoleSubsumptions.add(new Pair<OWLSubObjectPropertyOfAxiom, Integer>(roleSubsumptions.get(i), -1));			
		}
		
		weightedRoleEquivalences = new ArrayList<Pair<OWLEquivalentObjectPropertiesAxiom, Integer>>();
		for(int i = 0; i < roleEquivalences.size(); i++) {
			 weightedRoleEquivalences.add(new Pair<OWLEquivalentObjectPropertiesAxiom, Integer>(roleEquivalences.get(i), -1));			
		}
		
	}
	
	public List<Pair<OWLSubClassOfAxiom, Integer>> getSubsumptions() {
		return this.weightedSubsumptions;
	}
	
	public void changeSubsumptionWeight(int weight, int index) {
		Pair<OWLSubClassOfAxiom, Integer> newWeightedAxiom = new Pair<OWLSubClassOfAxiom, Integer>(this.weightedSubsumptions.get(index).getValue0(), weight); // Create a new pair with the updates weight
		this.weightedSubsumptions.set(index, newWeightedAxiom);
	}
	
	public int getSubsumptionWeight(int index) {
		return this.weightedSubsumptions.get(index).getValue1();
	}
	
	public List<Pair<OWLDisjointClassesAxiom, Integer>> getDisjunctions() {
		return this.weightedDisjunctions;
	}
	
	public void changeDisjunctionWeight(int weight, int index) {
		Pair<OWLDisjointClassesAxiom, Integer> newWeightedAxiom = new Pair<OWLDisjointClassesAxiom, Integer>(this.weightedDisjunctions.get(index).getValue0(), weight); // Create a new pair with the updates weight
		this.weightedDisjunctions.set(index, newWeightedAxiom);
	}
	
	public List<Pair<OWLEquivalentClassesAxiom, Integer>> getEquivalences() {
		return this.weightedEquivalences;
	}
	
	public void changeEquivalenceWeight(int weight, int index) {
		Pair<OWLEquivalentClassesAxiom, Integer> newWeightedAxiom = new Pair<OWLEquivalentClassesAxiom, Integer>(this.weightedEquivalences.get(index).getValue0(), weight); // Create a new pair with the updates weight
		this.weightedEquivalences.set(index, newWeightedAxiom);
	}
	
	public int getEquivalenceWeight(int index) {
		return this.weightedEquivalences.get(index).getValue1();
	}
	
	public List<Pair<OWLSubObjectPropertyOfAxiom, Integer>> getRoleSubsumptions(){
		return this.weightedRoleSubsumptions;
	}
	
	public void changeRoleSubsumptionWeight(int weight, int index) {
		Pair<OWLSubObjectPropertyOfAxiom, Integer> newWeightedAxiom = new Pair<OWLSubObjectPropertyOfAxiom, Integer>(this.weightedRoleSubsumptions.get(index).getValue0(), weight); // Create a new pair with the updates weight
		this.weightedRoleSubsumptions.set(index, newWeightedAxiom);
	}
	
	public int getRoleSubsumptionWeight(int index) {
		return this.weightedRoleSubsumptions.get(index).getValue1();
	}
	
	public List<Pair<OWLEquivalentObjectPropertiesAxiom, Integer>> getRoleEquivalences(){
		return this.weightedRoleEquivalences;
	}
	
	public void changeRoleEquivalenceWeight(int weight, int index) {
		Pair<OWLEquivalentObjectPropertiesAxiom, Integer> newWeightedAxiom = new Pair<OWLEquivalentObjectPropertiesAxiom, Integer>(this.weightedRoleEquivalences.get(index).getValue0(), weight); // Create a new pair with the updates weight
		this.weightedRoleEquivalences.set(index, newWeightedAxiom);
	}
	
	public int getRoleEquivalenceWeight(int index) {
		return this.weightedRoleEquivalences.get(index).getValue1();
	}

	
	
	
}
