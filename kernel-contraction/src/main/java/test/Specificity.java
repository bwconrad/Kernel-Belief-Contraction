package test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

public class Specificity {
	public static boolean loop = false;
	
	// Create a list of ontology axioms with their specificiy weightings
	public static WeightedAxioms getWeightedAxioms(OWLOntology elOntology, OWLOntologyManager ontologyManager, OWLDataFactory df) throws OWLOntologyCreationException {
		// Separate logical axioms into tbox
		Set<OWLLogicalAxiom> logicalAxioms = elOntology.getLogicalAxioms();
		List<OWLLogicalAxiom> tbox = new ArrayList<OWLLogicalAxiom>(logicalAxioms);
		
		// Create lists of each axiom type 
		Set<OWLSubClassOfAxiom> subsumptionsSet = elOntology.getAxioms(AxiomType.SUBCLASS_OF);
		List<OWLSubClassOfAxiom> subsumptions = new ArrayList<OWLSubClassOfAxiom>(subsumptionsSet);
					
		Set<OWLDisjointClassesAxiom> disjunctionsSet = elOntology.getAxioms(AxiomType.DISJOINT_CLASSES);
		List<OWLDisjointClassesAxiom> disjunctions = new ArrayList<OWLDisjointClassesAxiom>(disjunctionsSet);
	
		Set<OWLEquivalentClassesAxiom> equivalencesSet = elOntology.getAxioms(AxiomType.EQUIVALENT_CLASSES);
		List<OWLEquivalentClassesAxiom> equivalences = new ArrayList<OWLEquivalentClassesAxiom>(equivalencesSet);
		
		Set<OWLSubObjectPropertyOfAxiom> roleSubsumptionsSet = elOntology.getAxioms(AxiomType.SUB_OBJECT_PROPERTY);
		List<OWLSubObjectPropertyOfAxiom> roleSubsumptions = new ArrayList<OWLSubObjectPropertyOfAxiom>(roleSubsumptionsSet);
		
		Set<OWLEquivalentObjectPropertiesAxiom> roleEquivalencesSet = elOntology.getAxioms(AxiomType.EQUIVALENT_OBJECT_PROPERTIES);
		List<OWLEquivalentObjectPropertiesAxiom> roleEquivalences = new ArrayList<OWLEquivalentObjectPropertiesAxiom>(roleEquivalencesSet);
		
			
		// Get lists of classes, existentials and individuals
		List<Pair<OWLClassExpression, List<OWLClassExpression>>> classes = getClasses(elOntology);
		List<Pair<OWLObjectProperty, List<OWLObjectProperty>>> roles = getRoles(elOntology);
		List<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>> existentialExpressions = getExistentials(tbox, df); // Complex expressions using properties and classes

		
		// Add children to classes and existentials
		addChildren(classes, existentialExpressions, roles, subsumptions, disjunctions, equivalences, roleSubsumptions, roleEquivalences, df);
		
		// Add specificity weights to each axiom
		WeightedAxioms weightedAxioms = new WeightedAxioms(subsumptions, disjunctions, equivalences, roleSubsumptions, roleEquivalences);
		
		// Assign weights
		assignWeights(weightedAxioms, classes, existentialExpressions, roles, ontologyManager, df);
		
		
		
		return weightedAxioms;
	
	}
	

	// Generate list of classes with empty children lists 
	public static List<Pair<OWLClassExpression, List<OWLClassExpression>>> getClasses(OWLOntology ontology) {
		List<OWLClass> classes = new ArrayList<OWLClass>(ontology.getClassesInSignature()); // Get all classes 
		Set<Pair<OWLClassExpression, List<OWLClassExpression>>> classExpressions = new HashSet<Pair<OWLClassExpression, List<OWLClassExpression>>>();
				 
		// Convert class objects into class expressions
		for(OWLClass c : classes) {
			Pair<OWLClassExpression, List<OWLClassExpression>> item = new Pair<OWLClassExpression, List<OWLClassExpression>>(c.getNestedClassExpressions().iterator().next(), new ArrayList<OWLClassExpression>());
			classExpressions.add(item);
		}
				 
		return new ArrayList<Pair<OWLClassExpression, List<OWLClassExpression>>>(classExpressions);
	}

	// Generate list of roles with empty children lists 
	private static List<Pair<OWLObjectProperty, List<OWLObjectProperty>>> getRoles(OWLOntology ontology) {
		List<OWLObjectProperty> roles = new ArrayList<OWLObjectProperty>(ontology.getObjectPropertiesInSignature()); // Get all roles
		Set<Pair<OWLObjectProperty, List<OWLObjectProperty>>> roleExpressions = new HashSet<Pair<OWLObjectProperty, List<OWLObjectProperty>>>();
				 
		// Convert class objects into class expressions
		for(OWLObjectProperty r : roles) {
			Pair<OWLObjectProperty, List<OWLObjectProperty>> item = new Pair<OWLObjectProperty, List<OWLObjectProperty>>(r, new ArrayList<OWLObjectProperty>());
			roleExpressions.add(item);
		}
				 
		return new ArrayList<Pair<OWLObjectProperty, List<OWLObjectProperty>>>(roleExpressions);
	}
	
	// Generate list of existenital expressions from tbox
	public static List<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>> getExistentials(List<OWLLogicalAxiom> tbox, OWLDataFactory df) {
		Set<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>> existentials = new HashSet<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>>(); // List of existentials in tbox
		Set<OWLClassExpression> axiomContents = new HashSet<OWLClassExpression>(); // Contents of an axiom
		Class<? extends OWLObjectSomeValuesFrom> existentialClass = df.getOWLObjectSomeValuesFrom(df.getOWLBottomObjectProperty(), df.getOWLThing()).getClass(); // Class name of existential axioms
				 		
		// Go through all axioms in tbox
		for(OWLLogicalAxiom axiom : tbox) {
			axiomContents = axiom.getNestedClassExpressions();
					 
			// Check each expression in axiom if it is an existential
			for(OWLClassExpression exp : axiomContents) {
				if(exp.getClass().equals(existentialClass)) {
					// Create a pair with the existential and a children list
					List<OWLClassExpression> empty = new ArrayList<OWLClassExpression>();
					
					Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> item = new Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>(exp, new ArrayList<OWLClassExpression>(), new ArrayList<OWLClassExpression>()); 
					existentials.add(item);
				}
			}
		}
		return new ArrayList<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>>(existentials);
	}

	// Add axiom children to classes ,existential expressions and roles
	public static void addChildren(List<Pair<OWLClassExpression, List<OWLClassExpression>>> classes,
				List<Triplet<OWLClassExpression,List<OWLClassExpression>,List<OWLClassExpression>>> existentialExpressions,
				List<Pair<OWLObjectProperty, List<OWLObjectProperty>>> roles,
				List<OWLSubClassOfAxiom> subsumptions,
				List<OWLDisjointClassesAxiom> disjunctions,
				List<OWLEquivalentClassesAxiom> equivalences, 
				List<OWLSubObjectPropertyOfAxiom> roleSubsumptions, 
				List<OWLEquivalentObjectPropertiesAxiom> roleEquivalences,
				OWLDataFactory df) {

		// Loop through all subsumption axioms
		for(OWLSubClassOfAxiom axiom : subsumptions) {
			
			// Get the axiom parent
			List<OWLClassExpression> fullParent = new ArrayList<OWLClassExpression>(axiom.getSuperClass().getNestedClassExpressions());
			OWLClassExpression parent = null;
			if(fullParent.size() == 1) {
				parent = fullParent.get(0);
			}
			else {
				int i = 0;
				for(OWLClassExpression exp : fullParent) {
					if(exp.getObjectPropertiesInSignature().size() != 0) {
						parent = fullParent.get(i);
					}
					i++;
				}
			}

			// Get the axiom child
			List<OWLClassExpression> fullChild = new ArrayList<OWLClassExpression>(axiom.getSubClass().getNestedClassExpressions());
			OWLClassExpression child = null;
			if(fullChild.size() == 1) {
				child = fullChild.get(0);
			}
			else {
				int i = 0;
				for(OWLClassExpression exp : fullChild) {
					if(exp.getObjectPropertiesInSignature().size() != 0) {
						child = fullChild.get(i);
					}
					i++;
				}
			}
			
			try {
				parent.asOWLClass(); // Check if class or existential
				// If class
				for(Pair<OWLClassExpression, List<OWLClassExpression>> c : classes) {
					// Find parent instance
					if(c.getValue0().equals(parent)) {
						c.getValue1().add(child); // Add new child
						break;
					}
				}
				continue;
			}
			catch (OWLRuntimeException error) {
				// If existential
				
				for(Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> e : existentialExpressions) {	
					// Find parent instance
					if(e.getValue0().equals(parent)) {
						
						e.getValue1().add(child); // Add new child
						break;
					}
				}
				
			continue;
			}
		}
		
		// Loop through all equivalence axioms
		for(OWLEquivalentClassesAxiom axiom : equivalences) {
			// Add all other elements in the equivalences into each of the elements child list
			for(OWLClassExpression parent : axiom.getClassExpressionsAsList()) {
				for(OWLClassExpression child : axiom.getClassExpressionsAsList()) {
					
					// Add the element to the parent's child list
					if(child != parent) {
						try {
							parent.asOWLClass(); // Check if class or existential
							
							// If class
							for(Pair<OWLClassExpression, List<OWLClassExpression>> c : classes) {
								// Find parent instance
								if(c.getValue0().equals(parent)) {
									// Only add if not already in child list
									if(!c.getValue1().contains(child)) {
										c.getValue1().add(child); // Add new child
										break;
									}
								}
							}
							continue;
						}
						catch (OWLRuntimeException error) {
							// If existential
							for(Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> e : existentialExpressions) {	
								// Find parent instance
								if(e.getValue0().equals(parent)) {
									// Only add if not already in child list
									if(!e.getValue1().contains(child)) {
										e.getValue1().add(child); // Add new child
										break;
									}
								}
							}
						continue;
						}
					}
				}
			}	
		}
		
		// Loop through all role subsumption axioms
		for(OWLSubObjectPropertyOfAxiom axiom : roleSubsumptions) {
			// Get the axiom parent
			OWLObjectPropertyExpression parent = axiom.getSuperProperty();

			// Get the axiom child
			OWLObjectPropertyExpression child = axiom.getSubProperty();
				

			for(Pair<OWLObjectProperty, List<OWLObjectProperty>> r : roles) {
				// Find parent instance
				if(r.getValue0().equals(parent)) {
					r.getValue1().add(child.asOWLObjectProperty()); // Add new child
					break;
				}
			}
			continue;
		}	
		
		// Loop through all role equivalence axioms
		for(OWLEquivalentObjectPropertiesAxiom axiom : roleEquivalences) {
			// Add all other elements in the equivalences into each of the elements child list
			for(OWLObjectProperty parent : axiom.getObjectPropertiesInSignature()) {
				for(OWLObjectProperty child : axiom.getObjectPropertiesInSignature()) {
					// Add the element to the parent's child list
					if(child != parent) {
						for(Pair<OWLObjectProperty, List<OWLObjectProperty>> r : roles) {
							// Find parent instance
							if(r.getValue0().equals(parent)) {
								// Only add if not already in child list
								if(!r.getValue1().contains(child)) {
									r.getValue1().add(child.asOWLObjectProperty()); // Add new child
									break;
								}
							}
						}
						continue;
					}
				}
			}	
		}
		addIndirectChildren(classes, existentialExpressions, roles, subsumptions, equivalences, roleSubsumptions, roleEquivalences, df);
	}

	// Add indirect axiom children to existential expressions
	private static void addIndirectChildren(List<Pair<OWLClassExpression, List<OWLClassExpression>>> classes,
			List<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>> existentialExpressions,
			List<Pair<OWLObjectProperty, List<OWLObjectProperty>>> roles, List<OWLSubClassOfAxiom> subsumptions,
			List<OWLEquivalentClassesAxiom> equivalences, List<OWLSubObjectPropertyOfAxiom> roleSubsumptions,
			List<OWLEquivalentObjectPropertiesAxiom> roleEquivalences, 
			OWLDataFactory df) {
		
		// Loop through all subsumption axioms
		for(OWLSubClassOfAxiom axiom : subsumptions) {
			// Get the axiom child
			if(axiom.getSubClass().getObjectPropertiesInSignature().size() != 0) {
				
				
				List<OWLClassExpression> fullChild = new ArrayList<OWLClassExpression>(axiom.getSubClass().getNestedClassExpressions());
				
				OWLClassExpression childExpression = null;
				if(fullChild.get(0).getObjectPropertiesInSignature().size() != 0) {
					childExpression = fullChild.get(0);
				}
				else {
					int i = 0;
					for(OWLClassExpression exp : fullChild) {
						if(exp.getObjectPropertiesInSignature().size() != 0) {
							childExpression= fullChild.get(i);
						}
						i++;
					}
				}
				
				Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> axiomExpression = null;
				
				// Find existential expression in list
				for(Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> e : existentialExpressions) {	
					if(e.getValue0().equals(childExpression)) {
						axiomExpression = e;
					}
				}
				
				// Get the class and role of the child
				OWLClass axiomClass = axiom.getSubClass().getClassesInSignature().iterator().next();
				OWLObjectProperty axiomRole = axiom.getSubClass().getObjectPropertiesInSignature().iterator().next();

				// Get all children of the class
				List<OWLClassExpression> classChildren = new ArrayList<OWLClassExpression>();
				classChildren.add(axiomClass);
				getClassChildren(classChildren, axiomClass, classes);
				
				// Get all children of the role
				List<OWLObjectProperty> roleChildren = new ArrayList<OWLObjectProperty>();
				roleChildren.add(axiomRole);
				getRoleChildren(roleChildren, axiomRole, roles);
				
				
				// Create existential expressions among all classes and roles
				OWLObjectSomeValuesFrom  newExpression;
				List<OWLObjectSomeValuesFrom> g = new ArrayList<OWLObjectSomeValuesFrom>();
				for(int i=0; i<classChildren.size(); i++) {
					for(int j=0; j<roleChildren.size(); j++) {
						newExpression = df.getOWLObjectSomeValuesFrom(roleChildren.get(j), classChildren.get(i));
						if(!newExpression.equals(axiomExpression.getValue0())) {
							axiomExpression.getValue2().add(newExpression);
						}
					}
				}
			}
		}
		
		// Loop through all equivalence axioms
		for(OWLEquivalentClassesAxiom axiom : equivalences) {
			// Get the first axiom child
			if(axiom.getClassExpressionsAsList().get(0).getObjectPropertiesInSignature().size() != 0) {
				
				OWLClassExpression childExpression = axiom.getClassExpressionsAsList().get(0);
				
				Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> axiomExpression = null;
				
				// Find existential expression in list
				for(Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> e : existentialExpressions) {	
					if(e.getValue0().equals(childExpression)) {
						axiomExpression = e;
					}
				}
				
				// Get the class and role of the child
				OWLClass axiomClass = axiom.getClassesInSignature().iterator().next();
				OWLObjectProperty axiomRole = axiom.getObjectPropertiesInSignature().iterator().next();

				// Get all children of the class
				List<OWLClassExpression> classChildren = new ArrayList<OWLClassExpression>();
				classChildren.add(axiomClass);
				getClassChildren(classChildren, axiomClass, classes);
				
				// Get all children of the role
				List<OWLObjectProperty> roleChildren = new ArrayList<OWLObjectProperty>();
				roleChildren.add(axiomRole);
				getRoleChildren(roleChildren, axiomRole, roles);

				// Create existential expressions among all classes and roles and add to children list
				OWLObjectSomeValuesFrom  newExpression;
				List<OWLObjectSomeValuesFrom> g = new ArrayList<OWLObjectSomeValuesFrom>();
				for(int i=0; i<classChildren.size(); i++) {
					for(int j=0; j<roleChildren.size(); j++) {
						newExpression = df.getOWLObjectSomeValuesFrom(roleChildren.get(j), classChildren.get(i));
						if(!newExpression.equals(axiomExpression.getValue0())) {
							axiomExpression.getValue2().add(newExpression);
						}
					}
				}
			}
			
			// Get the second axiom child
			if(axiom.getClassExpressionsAsList().get(1).getObjectPropertiesInSignature().size() != 0) {
						
				List<OWLClassExpression> fullChild = new ArrayList<OWLClassExpression>(axiom.getClassExpressionsAsList().get(1).getNestedClassExpressions());
				
				OWLClassExpression childExpression = null;
				if(fullChild.get(0).getObjectPropertiesInSignature().size() != 0) {
					childExpression = fullChild.get(0);
				}
				else {
					int i = 0;
					for(OWLClassExpression exp : fullChild) {
						if(exp.getObjectPropertiesInSignature().size() != 0) {
							childExpression= fullChild.get(i);
						}
						i++;
					}
				}
				
				Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> axiomExpression = null;
							
				// Find existential expression in list
				for(Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> e : existentialExpressions) {	
					if(e.getValue0().equals(childExpression)) {
						axiomExpression = e;
					}
				}
				
				// Get the class and role of the child
				OWLClass axiomClass = axiom.getClassesInSignature().iterator().next();
				OWLObjectProperty axiomRole = axiom.getObjectPropertiesInSignature().iterator().next();

				// Get all children of the class
				List<OWLClassExpression> classChildren = new ArrayList<OWLClassExpression>();
				classChildren.add(axiomClass);
				getClassChildren(classChildren, axiomClass, classes);
							
				// Get all children of the role
				List<OWLObjectProperty> roleChildren = new ArrayList<OWLObjectProperty>();
				roleChildren.add(axiomRole);
				getRoleChildren(roleChildren, axiomRole, roles);
	
				// Create existential expressions among all classes and roles and add to children list
				OWLObjectSomeValuesFrom  newExpression;
				List<OWLObjectSomeValuesFrom> g = new ArrayList<OWLObjectSomeValuesFrom>();
				for(int i=0; i<classChildren.size(); i++) {
					for(int j=0; j<roleChildren.size(); j++) {
						newExpression = df.getOWLObjectSomeValuesFrom(roleChildren.get(j), classChildren.get(i));
						if(!newExpression.equals(axiomExpression.getValue0())) {
							axiomExpression.getValue2().add(newExpression);
						}
					}
				}
			}
		}
	}

	// Get the recursive children of a role
	private static void getRoleChildren(List<OWLObjectProperty> roleChildren, OWLObjectProperty axiomRole,
			List<Pair<OWLObjectProperty, List<OWLObjectProperty>>> roles) {

		// Match class with class list
		for(Pair<OWLObjectProperty, List<OWLObjectProperty>> r : roles) {
			if(r.getValue0().equals(axiomRole)) {
				// Add all children to list
				for(OWLObjectProperty child : r.getValue1()) {
					// Only add if child is a role
					if(child.getObjectPropertiesInSignature().size() == 1 && !roleChildren.contains(child)) {
						roleChildren.add(child);
						getRoleChildren(roleChildren, child, roles);
					}
				}
				break;
			}
		}
		
	}

	// Get the recursive children of a class
	private static void getClassChildren(List<OWLClassExpression> classChildren, OWLClassExpression axiomClass,
			List<Pair<OWLClassExpression, List<OWLClassExpression>>> classes) {
		
		// Match class with class list
		for(Pair<OWLClassExpression, List<OWLClassExpression>> c : classes) {
			if(c.getValue0().equals(axiomClass)) {
				// Add all children to list
				for(OWLClassExpression child : c.getValue1()) {
					// Only add if child is a class
					if(child.getObjectPropertiesInSignature().size() == 0 && !classChildren.contains(child)) {
						classChildren.add(child);
						getClassChildren(classChildren, child, classes);
					}
				}
				break;
			}
		}
		
	}

	// Assign weights to all axioms
	public static void assignWeights(WeightedAxioms weightedAxioms,
			  List<Pair<OWLClassExpression, List<OWLClassExpression>>> classes,
			  List<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>> existentialExpressions,
			  List<Pair<OWLObjectProperty, List<OWLObjectProperty>>> roles,
			  OWLOntologyManager manager, 
			  OWLDataFactory df) {

		//// Hierarchy Weighing
		// Subsumptions
		int index = 0;
		for(Pair<OWLSubClassOfAxiom, Integer> axiom : weightedAxioms.getSubsumptions()) {
			int weight = getSubsumptionWeight(weightedAxioms, axiom, classes, existentialExpressions, weightedAxioms.getSubsumptions(), weightedAxioms.getEquivalences(), manager, df, 
											  new HashSet<OWLSubClassOfAxiom>(), new HashSet<OWLEquivalentClassesAxiom>());

			weightedAxioms.changeSubsumptionWeight(weight, index);
			index++;	
		}
		
		
		// Disjunctions
		index = 0;
		for(Pair<OWLDisjointClassesAxiom, Integer> axiom : weightedAxioms.getDisjunctions()) {
			int weight = getDisjunctionWeight(weightedAxioms, axiom, classes, existentialExpressions, weightedAxioms.getSubsumptions(), weightedAxioms.getEquivalences(), manager, df,
											  new HashSet<OWLSubClassOfAxiom>(), new HashSet<OWLEquivalentClassesAxiom>());
			weightedAxioms.changeDisjunctionWeight(weight, index);
			index++;	
		}
		
		// Equivalences
		index = 0;
		for(Pair<OWLEquivalentClassesAxiom, Integer> axiom : weightedAxioms.getEquivalences()) {
			int weight = getEquivalenceWeight(weightedAxioms, axiom, classes, existentialExpressions, weightedAxioms.getSubsumptions(), weightedAxioms.getEquivalences(), manager, df, 
											  new HashSet<OWLSubClassOfAxiom>(), new HashSet<OWLEquivalentClassesAxiom>());
			weightedAxioms.changeEquivalenceWeight(weight, index);
			index++;	
		}
		
		// Create weighted roles
		List<Pair<Pair<OWLObjectProperty, List<OWLObjectProperty>>, Integer>> weightedRoles = new ArrayList<Pair<Pair<OWLObjectProperty, List<OWLObjectProperty>>, Integer>>();
		for(int i = 0; i<roles.size(); i++) {
			weightedRoles.add(new Pair<Pair<OWLObjectProperty, List<OWLObjectProperty>>, Integer>(roles.get(i), -1));
		}
		
		// Role Subsumptions
		index = 0;
		for(Pair<OWLSubObjectPropertyOfAxiom, Integer> axiom : weightedAxioms.getRoleSubsumptions()) {
			int weight = getRoleSubsumptionWeight(weightedAxioms, axiom, roles, weightedAxioms.getRoleSubsumptions(), weightedAxioms.getRoleEquivalences(), manager, df, 
													new HashSet<OWLSubObjectPropertyOfAxiom>(), new HashSet<OWLEquivalentObjectPropertiesAxiom>());
			weightedAxioms.changeRoleSubsumptionWeight(weight, index);
			index++;	
		}
		
		// Role Equivalences
		index = 0;
		for(Pair<OWLEquivalentObjectPropertiesAxiom, Integer> axiom : weightedAxioms.getRoleEquivalences()) {
			// Get weight using both methods and take the max
			int weight = getRoleEquivalenceWeight(weightedAxioms, axiom, roles, weightedAxioms.getRoleSubsumptions(), weightedAxioms.getRoleEquivalences(), manager, df, 
												    new HashSet<OWLSubObjectPropertyOfAxiom>(), new HashSet<OWLEquivalentObjectPropertiesAxiom>());
			weightedAxioms.changeRoleEquivalenceWeight(weight, index);
			index++;	
		}
		

		//// Indirect Weighing
		// Subsumptions
		index = 0;
		for(Pair<OWLSubClassOfAxiom, Integer> axiom : weightedAxioms.getSubsumptions()) {
			// Check if both sides are only classes
			if(axiom.getValue0().getSubClass().getObjectPropertiesInSignature().size() == 0 &&
			   axiom.getValue0().getSuperClass().getObjectPropertiesInSignature().size() == 0 ) {
				int weight = getSubsumptionWeightIndirect(axiom, weightedAxioms);  // Get indirect weight
				// Update if indirect weight is larger
				if(weightedAxioms.getSubsumptionWeight(index) < weight) {
					weightedAxioms.changeSubsumptionWeight(weight, index);
				}
			}
			index++;	
		}
			
		
		// Equivalences
		index = 0;
		for(Pair<OWLEquivalentClassesAxiom, Integer> axiom : weightedAxioms.getEquivalences()) {
			// Check if both sides are only classes
			if(axiom.getValue0().getObjectPropertiesInSignature().size() == 0 ) {
				int weight = getEquivalenceWeightIndirect(axiom, weightedAxioms);  // Get indirect weight
				// Update if indirect weight is larger
				if(weightedAxioms.getEquivalenceWeight(index) < weight) {
					weightedAxioms.changeEquivalenceWeight(weight, index);
				}
			}
			index++;	
		}
		
		
		// Role Subsumptions
		index = 0;
		for(Pair<OWLSubObjectPropertyOfAxiom, Integer> axiom : weightedAxioms.getRoleSubsumptions()) {
			// Check if both sides are only classes
			int weight = getRoleSubsumptionWeightIndirect(axiom, weightedAxioms);  // Get indirect weight
			// Update if indirect weight is larger
			if(weightedAxioms.getRoleSubsumptionWeight(index) < weight) {
				weightedAxioms.changeRoleSubsumptionWeight(weight, index);	
			}
			index++;	
		}
		
		
		// Role Equivalences
		index = 0;
		for(Pair<OWLEquivalentObjectPropertiesAxiom, Integer> axiom : weightedAxioms.getRoleEquivalences()) {
			// Check if both sides are only classes
			int weight = getRoleEquivalencesWeightIndirect(axiom, weightedAxioms);  // Get indirect weight
			// Update if indirect weight is larger
			if(weightedAxioms.getRoleEquivalenceWeight(index) < weight) {
				weightedAxioms.changeRoleEquivalenceWeight(weight, index);	
			}
			index++;	
		}

		/// Fix Weightings
		/*
		// Subsumptions
		index = 0;
		for(Pair<OWLSubClassOfAxiom, Integer> axiom : weightedAxioms.getSubsumptions()) {
			int weight = fixSubsumptionWeight(weightedAxioms, axiom, classes, existentialExpressions, weightedAxioms.getSubsumptions(), weightedAxioms.getEquivalences(), manager, df, 
											  new HashSet<OWLSubClassOfAxiom>(), new HashSet<OWLEquivalentClassesAxiom>());

			//weightedAxioms.changeSubsumptionWeight(weight, index);
			index++;	
		}
		System.out.println(weightedAxioms.getRoleSubsumptions());
		// Role Subsumptions
		index = 0;
		for(Pair<OWLSubObjectPropertyOfAxiom, Integer> axiom : weightedAxioms.getRoleSubsumptions()) {
			int weight = fixRoleSubsumptionWeight(weightedAxioms, axiom, roles, weightedAxioms.getRoleSubsumptions(), weightedAxioms.getRoleEquivalences(), manager, df, 
													new HashSet<OWLSubObjectPropertyOfAxiom>(), new HashSet<OWLEquivalentObjectPropertiesAxiom>());
			//weightedAxioms.changeRoleSubsumptionWeight(weight, index);
			index++;	
		}
		*/
		
	}


	// Get the weight of a subsumption axiom
	private static int getSubsumptionWeight(WeightedAxioms weightedAxioms, 
			 Pair<OWLSubClassOfAxiom, Integer> axiom,
			 List<Pair<OWLClassExpression, List<OWLClassExpression>>> classes,
			 List<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>> existentialExpressions, 
			 List<Pair<OWLSubClassOfAxiom, Integer>> subsumptions, 
			 List<Pair<OWLEquivalentClassesAxiom, Integer>> equivalences, 
			 OWLOntologyManager manager, 
			 OWLDataFactory df, 
			 Set<OWLSubClassOfAxiom> visitedSubsumptions, 
			 Set<OWLEquivalentClassesAxiom> visitedEquivalences) {
		
		loop = false;
	    int weight = 0;
		// If a weight has not been assignment yet
		if(axiom.getValue1() == -1) {
			OWLClassExpression subClass = axiom.getValue0().getSubClass(); 
			// Get the children of the sub class if they exist
			boolean hasChildren = false;
			for(Pair<OWLClassExpression, List<OWLClassExpression>> c : classes) { // Sub class is a class
				if(c.getValue0().equals(subClass) && !c.getValue1().isEmpty()) {
					hasChildren = true;
					visitedSubsumptions.add(axiom.getValue0());
					weight = 1 + getMaxWeight(weightedAxioms, c, subsumptions, equivalences, classes, existentialExpressions, manager, df, visitedSubsumptions, visitedEquivalences);
				}
			}

			for(Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> e : existentialExpressions) { // Sub class is an existential
				if(e.getValue0().equals(subClass) && (!e.getValue1().isEmpty() || !e.getValue2().isEmpty())) {
					hasChildren = true;
					visitedSubsumptions.add(axiom.getValue0());
					weight = 1 + getMaxWeightExist(weightedAxioms, e, subsumptions, equivalences, classes, existentialExpressions, manager, df, visitedSubsumptions, visitedEquivalences);
				}
			}

			// If sub class has no children
			if(hasChildren == false) {
				weight = 0;
			}
			
			// Assign weight to axiom
			int i = 0;
			for(Pair<OWLSubClassOfAxiom, Integer> weightedAxiom : weightedAxioms.getSubsumptions()) {
				if(weightedAxiom.equals(axiom) && weightedAxioms.getSubsumptionWeight(i) < weight) {
					weightedAxioms.changeSubsumptionWeight(weight, i);
				}
				i++;
			}
			
			// Loop detected -> change looping axioms weights to max weight found in loop
			// Subsumptions
			i = 0;
			if(loop == true) {
				for(Pair<OWLSubClassOfAxiom, Integer> weightedSub : weightedAxioms.getSubsumptions()) {
					for(OWLSubClassOfAxiom visitedSub : visitedSubsumptions) {
						if(visitedSub.equals(weightedSub.getValue0()) && weightedAxioms.getSubsumptionWeight(i) < weight) {
							//weightedAxioms.changeSubsumptionWeight(weight, i);
						}
					}
					i++;
				}
			}
			
			// Equivalences
			i = 0;
			if(loop == true) {
				for(Pair<OWLEquivalentClassesAxiom, Integer> weightedEq : weightedAxioms.getEquivalences()) {
					for(OWLEquivalentClassesAxiom visitedSub : visitedEquivalences) {
						if(visitedSub.equals(weightedEq.getValue0()) && weightedAxioms.getEquivalenceWeight(i) < weight) {
							//weightedAxioms.changeEquivalenceWeight(weight, i);
						}
					}
					i++;
				}
			}
			
			return weight;
		}

		else {
			return axiom.getValue1();
		}
	}
	
	// Get the indirect weight of a subsumption axiom
	// Both sides of the subsumption are only classes
	private static int getSubsumptionWeightIndirect(Pair<OWLSubClassOfAxiom,Integer> sub, WeightedAxioms weightedAxioms) {
		// Loop through all weighted axioms and find indirect connectins with sub
		int maxIndirectWeight = sub.getValue1();

		// Subsumptions
		for(Pair<OWLSubClassOfAxiom, Integer> weightedSub : weightedAxioms.getSubsumptions()) {
			Set<OWLClass> weightedSubClasses = weightedSub.getValue0().getSubClass().getClassesInSignature(); // Classes found in axiom subclass
			if(weightedSubClasses.contains(sub.getValue0().getSuperClass().getClassesInSignature().iterator().next()) && 
					weightedSub.getValue0().getSubClass().getDataPropertiesInSignature().size() != 0) {  // Check if subclass 
				if(weightedSub.getValue1() > maxIndirectWeight) {
					maxIndirectWeight = weightedSub.getValue1();
				}
			}
			
		}
		
		// Equivalences
		for(Pair<OWLEquivalentClassesAxiom, Integer> weightedEquiv : weightedAxioms.getEquivalences()) {
			Set<OWLClass> weightedSubClasses = weightedEquiv.getValue0().getClassesInSignature(); // Classes found in axiom 
			if(weightedSubClasses.contains(sub.getValue0().getSuperClass().getClassesInSignature().iterator().next())) {
				// Loop through each expression in equivalences and check if matching class is an existential expression
				for(OWLClassExpression exp : weightedEquiv.getValue0().getClassExpressions()) {
					if(exp.getObjectPropertiesInSignature().size() != 0 &&
					   exp.getClassesInSignature().contains(sub.getValue0().getSuperClass().getClassesInSignature().iterator().next())) {
						if(weightedEquiv.getValue1() > maxIndirectWeight) {
							maxIndirectWeight = weightedEquiv.getValue1();
						}
					}
				}
			}
			
		}
		
		// Disjunctions
		for(Pair<OWLDisjointClassesAxiom, Integer> weightedDis : weightedAxioms.getDisjunctions()) {
			Set<OWLClass> weightedSubClasses = weightedDis.getValue0().getClassesInSignature(); // Classes found in axiom 
			if(weightedSubClasses.contains(sub.getValue0().getSuperClass().getClassesInSignature().iterator().next())) {
				// Loop through each expression in disjunction and check if matching class is an existential expression
				for(OWLClassExpression exp : weightedDis.getValue0().getClassExpressions()) {
					if(exp.getObjectPropertiesInSignature().size() != 0 &&
					   exp.getClassesInSignature().contains(sub.getValue0().getSuperClass().getClassesInSignature().iterator().next())) {
						if(weightedDis.getValue1() > maxIndirectWeight) {
							maxIndirectWeight = weightedDis.getValue1();
						}
					}
				}
			}	
		}

		return maxIndirectWeight;
	}
	
	//TODO
	private static int fixSubsumptionWeight(WeightedAxioms weightedAxioms, 
			 							   Pair<OWLSubClassOfAxiom, Integer> axiom,
			 							   List<Pair<OWLClassExpression, List<OWLClassExpression>>> classes,
			 							   List<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>> existentialExpressions,
			 							   List<Pair<OWLSubClassOfAxiom, Integer>> subsumptions, 
			 							   List<Pair<OWLEquivalentClassesAxiom, Integer>> equivalences, 
			 							   OWLOntologyManager manager, 
			 							   OWLDataFactory df, 
			 							   Set<OWLSubClassOfAxiom> visitedSubsumptions, 
			 							   Set<OWLEquivalentClassesAxiom> visitedEquivalences) {
		//System.out.println("Ax: " + axiom);
		
		OWLClassExpression subClass = axiom.getValue0().getSubClass(); 
		int offset = 0;
		int weight;
		// Sub class is a class
		for(Pair<OWLClassExpression, List<OWLClassExpression>> c : classes) { 
			if(c.getValue0().equals(subClass) && !c.getValue1().isEmpty()) {
				visitedSubsumptions.add(axiom.getValue0());
				weight = getMaxWeight(weightedAxioms, c, subsumptions, equivalences, classes, existentialExpressions, manager, df, visitedSubsumptions, visitedEquivalences);
				//System.out.println("Class: " + c);
				//System.out.println("Weight: " + weight);
				
			}
		}
/*
		for(Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> e : existentialExpressions) { // Sub class is an existential
			if(e.getValue0().equals(subClass) && (!e.getValue1().isEmpty() || !e.getValue2().isEmpty())) {
				hasChildren = true;
				visitedSubsumptions.add(axiom.getValue0());
				weight = 1 + getMaxWeightExist(weightedAxioms, e, subsumptions, equivalences, classes, existentialExpressions, manager, df, visitedSubsumptions, visitedEquivalences);
			}
		}
*/
		return 0;
	}

	
	// Get the weight of a disjunction axiom
	private static int getDisjunctionWeight(WeightedAxioms weightedAxioms, 
			Pair<OWLDisjointClassesAxiom, Integer> axiom,
			List<Pair<OWLClassExpression, List<OWLClassExpression>>> classes,
			List<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>> existentialExpressions,
			List<Pair<OWLSubClassOfAxiom, Integer>> subsumptions, 
			List<Pair<OWLEquivalentClassesAxiom, Integer>> equivalences, 
			OWLOntologyManager manager, 
			OWLDataFactory df,
			Set<OWLSubClassOfAxiom> visitedSubsumptions, 
			Set<OWLEquivalentClassesAxiom> visitedEquivalences) {
		
		int weight;
		int maxWeight = 0;
		// If a weight has not been assignment yet
		if(axiom.getValue1() == -1) {
			List<OWLClassExpression> disjuncts = axiom.getValue0().getClassExpressionsAsList();
			
			// Find the max weight among all disjuncts
			for(OWLClassExpression disjunct : disjuncts) {
				weight = 0;
				// Get the children of the disjunct if they exist
				boolean hasChildren = false;
				for(Pair<OWLClassExpression, List<OWLClassExpression>> c : classes) { // Disjunct is a class
					if(c.getValue0().equals(disjunct) && !c.getValue1().isEmpty()) {
						hasChildren = true;
						weight = 1 + getMaxWeight(weightedAxioms, c, subsumptions, equivalences, classes, existentialExpressions, manager, df, visitedSubsumptions, visitedEquivalences);
					}
				}
	
				for(Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> e : existentialExpressions) { // Disjunct is an existential
					if(e.getValue0().equals(disjunct) && (!e.getValue1().isEmpty() || !e.getValue2().isEmpty())) {
						hasChildren = true;
						weight = 1 + getMaxWeightExist(weightedAxioms, e, subsumptions, equivalences, classes, existentialExpressions, manager, df, visitedSubsumptions, visitedEquivalences);
					}
				}
	
				// If sub class has no children
				if(hasChildren == false) {
					weight = 0;
				}
				
				// Update the max disjunct weight
				if(weight>maxWeight) {
					maxWeight = weight;
				}
			}

			// Assign weight to axiom
			int i = 0;
			for(Pair<OWLDisjointClassesAxiom, Integer> weightedAxiom : weightedAxioms.getDisjunctions()) {
				if(weightedAxiom.equals(axiom)) {
					weightedAxioms.changeDisjunctionWeight(maxWeight, i);
				}
				i++;
			}
			return maxWeight;
		}

		else {
			return axiom.getValue1();
		}
	}
	
	// Get the weight of an equivalence axiom
	private static int getEquivalenceWeight(WeightedAxioms weightedAxioms, 
			Pair<OWLEquivalentClassesAxiom, Integer> axiom,
			List<Pair<OWLClassExpression, List<OWLClassExpression>>> classes,
			List<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>> existentialExpressions,
			List<Pair<OWLSubClassOfAxiom, Integer>> subsumptions, 
			List<Pair<OWLEquivalentClassesAxiom,Integer>> equivalences, 
			OWLOntologyManager manager, OWLDataFactory df, 
			Set<OWLSubClassOfAxiom> visitedSubsumptions, 
			Set<OWLEquivalentClassesAxiom> visitedEquivalences) {
		
		int weight1 = 0;
		int weight2 = 0;
		// If a weight has not been assignment yet
		if(axiom.getValue1() == -1) {
			
			// Find axioms including the LHS class
			OWLClassExpression class1 = axiom.getValue0().getClassExpressionsAsList().get(0); 

			// Get the children of the sub class if they exist
			boolean hasChildren = false;
			for(Pair<OWLClassExpression, List<OWLClassExpression>> c : classes) { // Sub class is a class
				if(c.getValue0().equals(class1) && !c.getValue1().isEmpty()) {
					hasChildren = true;
					visitedEquivalences.add(axiom.getValue0());
					weight1 = 1 + getMaxWeight(weightedAxioms, c, subsumptions, equivalences, classes, existentialExpressions, manager, df, visitedSubsumptions, visitedEquivalences);
				}
			}

			for(Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> e : existentialExpressions) { // Sub class is an existential
				if(e.getValue0().equals(class1) && (!e.getValue1().isEmpty() || !e.getValue2().isEmpty())) {
					hasChildren = true;
					visitedEquivalences.add(axiom.getValue0());
					weight1 = 1 + getMaxWeightExist(weightedAxioms, e, subsumptions, equivalences, classes, existentialExpressions, manager, df, visitedSubsumptions, visitedEquivalences);
				}
			}

			// If sub class has no children
			if(hasChildren == false) {
				weight1 = 0;
			}
			
			// Find axioms including the RHS class
			OWLClassExpression class2 = axiom.getValue0().getClassExpressionsAsList().get(1); 
			
			// Get the children of the sub class if they exist
			hasChildren = false;
			for(Pair<OWLClassExpression, List<OWLClassExpression>> c : classes) { // Sub class is a class
				if(c.getValue0().equals(class2) && !c.getValue1().isEmpty()) {
					hasChildren = true;
					visitedEquivalences.add(axiom.getValue0());
					weight2 = 1 + getMaxWeight(weightedAxioms, c, subsumptions, equivalences, classes, existentialExpressions, manager, df, visitedSubsumptions, visitedEquivalences);
				}
			}

			for(Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> e : existentialExpressions) { // Sub class is an existential
				if(e.getValue0().equals(class2) && (!e.getValue1().isEmpty() || !e.getValue2().isEmpty())) {
					hasChildren = true;
					visitedEquivalences.add(axiom.getValue0());
					weight2 = 1 + getMaxWeightExist(weightedAxioms, e, subsumptions, equivalences, classes, existentialExpressions, manager, df, visitedSubsumptions, visitedEquivalences);
				}
			}

			// If sub class has no children
			if(hasChildren == false) {
				weight2 = 0;
			}
			
			// Assign weight to axiom
			int i = 0;
			for(Pair<OWLEquivalentClassesAxiom, Integer> weightedAxiom : weightedAxioms.getEquivalences()) {
				if(weightedAxiom.equals(axiom)) {
					weightedAxioms.changeEquivalenceWeight(Math.max(weight1, weight2), i);
				}
				i++;
			}
			
			return Math.max(weight1, weight2);
		}

		else {
			return axiom.getValue1();
		}
	}
	
	// Get the indirect weight of an equivalence axiom
	// Both sides are only classes
	private static int getEquivalenceWeightIndirect(Pair<OWLEquivalentClassesAxiom, Integer> eq,
			WeightedAxioms weightedAxioms) {
		// Loop through all weighted axioms and find indirect connectins with sub
		int maxIndirectWeight = eq.getValue1();
		
		for(OWLClass c : eq.getValue0().getClassesInSignature()) {
			// Subsumptions
			for(Pair<OWLSubClassOfAxiom, Integer> weightedSub : weightedAxioms.getSubsumptions()) {
				Set<OWLClass> weightedSubClasses = weightedSub.getValue0().getSubClass().getClassesInSignature(); // Classes found in axiom subclass
				if(weightedSubClasses.contains(c) && 
				   weightedSub.getValue0().getSubClass().getDataPropertiesInSignature().size() != 0) {
					if(weightedSub.getValue1() > maxIndirectWeight) {
						maxIndirectWeight = weightedSub.getValue1();
					}
				}			
			}
					
			// Equivalences
			for(Pair<OWLEquivalentClassesAxiom, Integer> weightedEquiv : weightedAxioms.getEquivalences()) {
				Set<OWLClass> weightedSubClasses = weightedEquiv.getValue0().getClassesInSignature(); // Classes found in axiom subclass
				if(weightedSubClasses.contains(c)) {
					// Loop through each expression in equivalences and check if matching class is an existential expression
					for(OWLClassExpression exp : weightedEquiv.getValue0().getClassExpressions()) {
						if(exp.getObjectPropertiesInSignature().size() != 0 &&
						   exp.getClassesInSignature().contains(c)) {
							if(weightedEquiv.getValue1() > maxIndirectWeight) {
								maxIndirectWeight = weightedEquiv.getValue1();
							}
						}
					}
					
				}
			}
					
			// Disjunctions
			for(Pair<OWLDisjointClassesAxiom, Integer> weightedDis : weightedAxioms.getDisjunctions()) {
				Set<OWLClass> weightedSubClasses = weightedDis.getValue0().getClassesInSignature(); // Classes found in axiom subclass
				if(weightedSubClasses.contains(c)) {
					// Loop through each expression in disjunction and check if matching class is an existential expression
					for(OWLClassExpression exp : weightedDis.getValue0().getClassExpressions()) {
						if(exp.getObjectPropertiesInSignature().size() != 0 &&
								   exp.getClassesInSignature().contains(c)) {
							
							if(weightedDis.getValue1() > maxIndirectWeight) {
								maxIndirectWeight = weightedDis.getValue1();
							}
						}
					}
				}
					
						
			}
		}
		return maxIndirectWeight;
	}
	
	// Get the weight of a role subsumption axiom using the RI method
	private static int getRoleSubsumptionWeight(WeightedAxioms weightedAxioms,
			Pair<OWLSubObjectPropertyOfAxiom, Integer> axiom,
			List<Pair<OWLObjectProperty,List<OWLObjectProperty>>> roles, 
			List<Pair<OWLSubObjectPropertyOfAxiom, Integer>> roleSubsumptions,
			List<Pair<OWLEquivalentObjectPropertiesAxiom, Integer>> roleEquivalences,
			OWLOntologyManager manager, 
			OWLDataFactory df,
			Set<OWLSubObjectPropertyOfAxiom> visitedRoleSubsumptions, 
			Set<OWLEquivalentObjectPropertiesAxiom> visitedRoleEquivalences) {
		
		int weight = 0;
		// If a weight has not been assignment yet
		if(axiom.getValue1() == -1) {
			
			OWLObjectPropertyExpression subProperty = axiom.getValue0().getSubProperty(); 
			// Get the children of the sub property if they exist
			boolean hasChildren = false;
			for(Pair<OWLObjectProperty, List<OWLObjectProperty>> r : roles) {
				if(r.getValue0().equals(subProperty) && !r.getValue1().isEmpty()) {
					hasChildren = true;
					visitedRoleSubsumptions.add(axiom.getValue0());
					weight = 1 + getMaxRoleWeight(weightedAxioms, r, roles, roleSubsumptions, roleEquivalences, 
												  manager, df, visitedRoleSubsumptions, visitedRoleEquivalences);
				}
			}

			// If sub class has no children
			if(hasChildren == false) {
				weight = 0;
			}
			
			// Assign weight to axiom
			int i = 0;
			for(Pair<OWLSubObjectPropertyOfAxiom, Integer> weightedAxiom : weightedAxioms.getRoleSubsumptions()) {
				if(weightedAxiom.equals(axiom)) {
					weightedAxioms.changeRoleSubsumptionWeight(weight, i);
				}
				i++;
			}
			
			return weight;
		}

		// Weight already assigned
		else {
			return axiom.getValue1();
		}
	}
	
	private static int getRoleSubsumptionWeightIndirect(Pair<OWLSubObjectPropertyOfAxiom, Integer> roleSub,
			WeightedAxioms weightedAxioms) {
		// Loop through all weighted axioms and find indirect connectins with sub
		int maxIndirectWeight = roleSub.getValue1();

		// Subsumptions
		for(Pair<OWLSubClassOfAxiom, Integer> weightedSub : weightedAxioms.getSubsumptions()) {
			Set<OWLObjectProperty> weightedSubRoles = weightedSub.getValue0().getSubClass().getObjectPropertiesInSignature(); // Properties found in axiom subclass
			
			if(weightedSubRoles.contains(roleSub.getValue0().getSuperProperty())) {
				if(weightedSub.getValue1() > maxIndirectWeight) {
					maxIndirectWeight = weightedSub.getValue1();
				}
			}		
		}
				
		// Equivalences
		for(Pair<OWLEquivalentClassesAxiom, Integer> weightedEquiv : weightedAxioms.getEquivalences()) {
			Set<OWLObjectProperty> weightedSubRoles= weightedEquiv.getValue0().getObjectPropertiesInSignature(); // Properties found in axiom subclass
			if(weightedSubRoles.contains(roleSub.getValue0().getSuperProperty())) {
				if(weightedEquiv.getValue1() > maxIndirectWeight) {
					maxIndirectWeight = weightedEquiv.getValue1();
				}
			}	
		}
				
		// Disjunctions
		for(Pair<OWLDisjointClassesAxiom, Integer> weightedDis : weightedAxioms.getDisjunctions()) {
			Set<OWLObjectProperty> weightedSubRoles= weightedDis.getValue0().getObjectPropertiesInSignature(); // Properties found in axiom subclass
			if(weightedSubRoles.contains(roleSub.getValue0().getSuperProperty())) {
				if(weightedDis.getValue1() > maxIndirectWeight) {
					maxIndirectWeight = weightedDis.getValue1();
				}
			}
		}
		
		return maxIndirectWeight;
	}

	
	private static int fixRoleSubsumptionWeight(WeightedAxioms weightedAxioms,
			Pair<OWLSubObjectPropertyOfAxiom, Integer> axiom,
			List<Pair<OWLObjectProperty, List<OWLObjectProperty>>> roles,
			List<Pair<OWLSubObjectPropertyOfAxiom, Integer>> roleSubsumptions,
			List<Pair<OWLEquivalentObjectPropertiesAxiom, Integer>> roleEquivalences,
			OWLOntologyManager manager,
			OWLDataFactory df,
			Set<OWLSubObjectPropertyOfAxiom> visitedRoleSubsumptions, 
			Set<OWLEquivalentObjectPropertiesAxiom> visitedRoleEquivalences) {
		
		System.out.println("Ax: " + axiom);
		OWLObjectPropertyExpression subProperty = axiom.getValue0().getSubProperty(); 
		int offset = 0;
		int weight = 0;
		
		
		for(Pair<OWLObjectProperty, List<OWLObjectProperty>> r : roles) {
			if(r.getValue0().equals(subProperty) && !r.getValue1().isEmpty()) {
				for(OWLObjectProperty child : r.getValue1()) {
					OWLSubObjectPropertyOfAxiom childAxiom = df.getOWLSubObjectPropertyOfAxiom(child, r.getValue0()); // Create subsumption to check weight of
					
					// Find subsumption in role subsumption list 
					for(Pair<OWLSubObjectPropertyOfAxiom, Integer> sub : weightedAxioms.getRoleSubsumptions()) {		
						if(sub.getValue0().equals(childAxiom)) {
							// Match the child role 
							Pair<OWLObjectProperty, List<OWLObjectProperty>> nextRole = null;
							for(Pair<OWLObjectProperty, List<OWLObjectProperty>> rr : roles) {
								if(child.equals(rr.getValue0())) {
									nextRole = rr;
								}
							}
							weight = getChildrenMaxRole(weightedAxioms, nextRole, roles, roleSubsumptions, roleEquivalences, manager,
									df, visitedRoleSubsumptions, visitedRoleEquivalences, weight);
						}
					}
					
				}
				
				System.out.println("Role: " + r);
				System.out.println("Weight: " + weight);
			}
		}
		
		
		return 0;
	}
	
	
	private static int getChildrenMaxRole(WeightedAxioms weightedAxioms,
			Pair<OWLObjectProperty, List<OWLObjectProperty>> role, 
			List<Pair<OWLObjectProperty,List<OWLObjectProperty>>> roles, 
			List<Pair<OWLSubObjectPropertyOfAxiom, Integer>> roleSubsumptions,
			List<Pair<OWLEquivalentObjectPropertiesAxiom,Integer>> roleEquivalences, 
			OWLOntologyManager manager,
			OWLDataFactory df, 
			Set<OWLSubObjectPropertyOfAxiom> visitedRoleSubsumptions,
			Set<OWLEquivalentObjectPropertiesAxiom> visitedRoleEquivalences,
			int maxWeight) {
		
		int weight = -1;
		// Loop through all children and find max weight among them
		for(OWLObjectProperty child : role.getValue1()) {
			// Find matching subsumptions
			OWLSubObjectPropertyOfAxiom childAxiom = df.getOWLSubObjectPropertyOfAxiom(child, role.getValue0()); // Create subsumption to check weight of
					 
			// Find subsumption in role subsumption list 
			for(Pair<OWLSubObjectPropertyOfAxiom, Integer> sub : roleSubsumptions) {		
				if(sub.getValue0().equals(childAxiom)) {
					// Check if axiom has been visit before -- detect looping
					if(!visitedRoleSubsumptions.contains(childAxiom)) {
						visitedRoleSubsumptions.add(childAxiom);
						// Match the child role 
						Pair<OWLObjectProperty, List<OWLObjectProperty>> nextRole = null;
						for(Pair<OWLObjectProperty, List<OWLObjectProperty>> r : roles) {
							if(sub.getValue0().getSubProperty().equals(r.getValue0())) {
								nextRole = r;
							}
						}
						// Get max weight of current axiom and of children axioms
						weight = Math.max(sub.getValue1(), getChildrenMaxRole(weightedAxioms, nextRole, roles, roleSubsumptions, roleEquivalences, manager,
								df, visitedRoleSubsumptions, visitedRoleEquivalences, maxWeight));
						if(weight > maxWeight) {
							maxWeight = weight;
						}
						
					}
				}
		
			}
		}
		
		return 0;
	}


	// Get the weight of a role equivalence axiom using the RI method
	private static int getRoleEquivalenceWeight(WeightedAxioms weightedAxioms,
			Pair<OWLEquivalentObjectPropertiesAxiom, Integer> axiom,
			List<Pair<OWLObjectProperty,List<OWLObjectProperty>>> roles, 
			List<Pair<OWLSubObjectPropertyOfAxiom,Integer>> roleSubsumptions, 
			List<Pair<OWLEquivalentObjectPropertiesAxiom,Integer>> roleEquivalences, 
			OWLOntologyManager manager, 
			OWLDataFactory df,
			Set<OWLSubObjectPropertyOfAxiom> visitedRoleSubsumptions,
			Set<OWLEquivalentObjectPropertiesAxiom> visitedRoleEquivalences) {
	
		int weight1 = 0;
		int weight2 = 0;
		// If a weight has not been assignment yet
		if(axiom.getValue1() == -1) {
			
			List<OWLObjectPropertyExpression> axiomRoleList = new ArrayList<OWLObjectPropertyExpression>(axiom.getValue0().getProperties());
			// Find axioms including the LHS class
			OWLObjectPropertyExpression role1 = axiomRoleList.get(0);
			// Get the children of the sub class if they exist
			boolean hasChildren = false;
			for(Pair<OWLObjectProperty, List<OWLObjectProperty>> r : roles) { 
				if(r.getValue0().equals(role1) && !r.getValue1().isEmpty()) {
					hasChildren = true;
					visitedRoleEquivalences.add(axiom.getValue0());
					weight1 = 1 + getMaxRoleWeight(weightedAxioms, r, roles, roleSubsumptions, roleEquivalences, 
							  				       manager, df, visitedRoleSubsumptions, visitedRoleEquivalences);
				}
			}

			// If sub class has no children
			if(hasChildren == false) {
				weight1 = 0;
			}
			
			// Find axioms including the RHS class
			OWLObjectPropertyExpression role2 = axiomRoleList.get(1);
			
			// Get the children of the sub class if they exist
			hasChildren = false;
			for(Pair<OWLObjectProperty, List<OWLObjectProperty>> r : roles) { // Sub class is a class
				if(r.getValue0().equals(role2) && !r.getValue1().isEmpty()) {
					hasChildren = true;
					visitedRoleEquivalences.add(axiom.getValue0());
					weight2 = 1 + getMaxRoleWeight(weightedAxioms, r, roles, roleSubsumptions, roleEquivalences, 
												   manager, df, visitedRoleSubsumptions, visitedRoleEquivalences);
				}
			}

			// If sub class has no children
			if(hasChildren == false) {
				weight2 = 0;
			}
			
			// Assign weight to axiom
			int i = 0;
			for(Pair<OWLEquivalentObjectPropertiesAxiom, Integer> weightedAxiom : weightedAxioms.getRoleEquivalences()) {
				if(weightedAxiom.equals(axiom)) {
					weightedAxioms.changeRoleEquivalenceWeight(Math.max(weight1, weight2), i);
				}
				i++;
			}
			
			return Math.max(weight1, weight2);
		}

		else {
			return axiom.getValue1();
		}
	}

	private static int getRoleEquivalencesWeightIndirect(Pair<OWLEquivalentObjectPropertiesAxiom, Integer> roleEq,
			WeightedAxioms weightedAxioms) {
		// Loop through all weighted axioms and find indirect connectins with sub
		int maxIndirectWeight = roleEq.getValue1();
				
		for(OWLObjectProperty p : roleEq.getValue0().getObjectPropertiesInSignature()) {
			// Subsumptions
			for(Pair<OWLSubClassOfAxiom, Integer> weightedSub : weightedAxioms.getSubsumptions()) {
				Set<OWLObjectProperty> weightedSubRoles = weightedSub.getValue0().getSubClass().getObjectPropertiesInSignature(); // Classes found in axiom subclass
				if(weightedSubRoles.contains(p)) {
					if(weightedSub.getValue1() > maxIndirectWeight) {
						maxIndirectWeight = weightedSub.getValue1();
					}
				}			
			}
							
			// Equivalences
			for(Pair<OWLEquivalentClassesAxiom, Integer> weightedEquiv : weightedAxioms.getEquivalences()) {
				Set<OWLObjectProperty> weightedSubRoles= weightedEquiv.getValue0().getObjectPropertiesInSignature(); // Classes found in axiom subclass
				if(weightedSubRoles.contains(p)) {
					if(weightedEquiv.getValue1() > maxIndirectWeight) {
						maxIndirectWeight = weightedEquiv.getValue1();
					}
				}
			}
							
			// Disjunctions
			for(Pair<OWLDisjointClassesAxiom, Integer> weightedDis : weightedAxioms.getDisjunctions()) {
				Set<OWLObjectProperty> weightedSubRoles= weightedDis.getValue0().getObjectPropertiesInSignature(); // Classes found in axiom subclass
				if(weightedSubRoles.contains(p)) {
					if(weightedDis.getValue1() > maxIndirectWeight) {
						maxIndirectWeight = weightedDis.getValue1();
					}
				}
						
			}
		}
		return maxIndirectWeight;
	}

	
	// Get the weight of a role equivalence axiom using the GCI method
	private static int getRoleEquivalenceWeightGCI(WeightedAxioms weightedAxioms,
			Pair<OWLEquivalentObjectPropertiesAxiom, Integer> axiom,
			List<Pair<Pair<OWLObjectProperty, List<OWLObjectProperty>>, Integer>> weightedRoles) {
		
		int max = -1;
		int weight;
		int index;
		// Get the max weight from all roles in the axiom
		for(OWLObjectPropertyExpression axiomRole : axiom.getValue0().getProperties()) {
			// Match the role within the role list
			index = 0;
			for(Pair<Pair<OWLObjectProperty, List<OWLObjectProperty>>, Integer> role : weightedRoles) {
				if(role.getValue0().getValue0().equals(axiomRole)) {
					weight = getWeightedRoleValue(role, weightedAxioms); // Get the weight of the role
					
					// Assign role weigh if unassigned
					if(role.getValue1() == -1) {
						Pair<Pair<OWLObjectProperty, List<OWLObjectProperty>>, Integer> newRole = new Pair<Pair<OWLObjectProperty, List<OWLObjectProperty>>, Integer>(role.getValue0(), weight);
						weightedRoles.set(index, newRole);
					}
					
					if(weight>max) {
						max = weight;
					}
				}
				index++;
			}
		}	
		return max;
	}
	
	private static int getRoleSubsumptionWeightGCI(WeightedAxioms weightedAxioms,
			Pair<OWLSubObjectPropertyOfAxiom, Integer> axiom,
			List<Pair<Pair<OWLObjectProperty, List<OWLObjectProperty>>, Integer>> weightedRoles) {
		int max = -1;
		int weight;
		int index;
		// Get the max weight from all roles in the axiom
		for(OWLObjectPropertyExpression axiomRole : axiom.getValue0().getObjectPropertiesInSignature()) {
			// Match the role within the role list
			index = 0;
			for(Pair<Pair<OWLObjectProperty, List<OWLObjectProperty>>, Integer> role : weightedRoles) {
				if(role.getValue0().getValue0().equals(axiomRole)) {
					weight = getWeightedRoleValue(role, weightedAxioms); // Get the weight of the role
					
					// Assign role weigh if unassigned
					if(role.getValue1() == -1) {
						Pair<Pair<OWLObjectProperty, List<OWLObjectProperty>>, Integer> newRole = new Pair<Pair<OWLObjectProperty, List<OWLObjectProperty>>, Integer>(role.getValue0(), weight);
						weightedRoles.set(index, newRole);
					}
					
					if(weight>max) {
						max = weight;
					}
				}
				index++;
			}
		}	
		return max;
	}
	

	// Find the max weight among all children of a class 
	private static int getMaxWeight(WeightedAxioms weightedAxioms, 
				Pair<OWLClassExpression, List<OWLClassExpression>> c, 
				List<Pair<OWLSubClassOfAxiom, Integer>> subsumptions, 
				List<Pair<OWLEquivalentClassesAxiom, Integer>> equivalences, 
				List<Pair<OWLClassExpression,List<OWLClassExpression>>> classes, 
				List<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>> existentialExpressions, 
				OWLOntologyManager manager, 
				OWLDataFactory df, 
				Set<OWLSubClassOfAxiom> visitedSubsumptions, 
				Set<OWLEquivalentClassesAxiom> visitedEquivalences) {

		int maxWeight = -1;
		int weight;
		// Loop through all children and find max weight among them
		for(OWLClassExpression child : c.getValue1()) {
			// Find matching subsumptions
			OWLSubClassOfAxiom childAxiom = df.getOWLSubClassOfAxiom(child, c.getValue0()); // Create subsumption to check weight of
			// Find subsumption in subsumption list 
			for(Pair<OWLSubClassOfAxiom, Integer> sub : subsumptions) {		
				if(sub.getValue0().equals(childAxiom)) {
					// Check if axiom has been visit before -- detect looping
					if(!visitedSubsumptions.contains(childAxiom)) {
						visitedSubsumptions.add(childAxiom);
						weight = Math.max(getSubsumptionWeight(weightedAxioms, sub, classes, existentialExpressions, subsumptions, equivalences, manager, df, visitedSubsumptions, visitedEquivalences),
										  getSubsumptionWeightIndirect(sub, weightedAxioms)); // Get the weight of the subsumption
						if(weight > maxWeight) {
							maxWeight = weight;
						}
					}
					else {
						loop = true;
					}
				}
			}
			
			// Find matching equivalence
			OWLEquivalentClassesAxiom childEquivalence= df.getOWLEquivalentClassesAxiom(child, c.getValue0()); // Create equivalence to check weight of
			
			// Find equivalence in equivalences list
			for(Pair<OWLEquivalentClassesAxiom, Integer> eq : equivalences) {		
				if(eq.getValue0().equals(childEquivalence)) {
					// Check if axiom has been visit before -- detect looping
					if(!visitedEquivalences.contains(childEquivalence)) {
						visitedEquivalences.add(childEquivalence);
						weight = getEquivalenceWeight(weightedAxioms, eq, classes, existentialExpressions, subsumptions, equivalences, manager, df, visitedSubsumptions, visitedEquivalences); // Get the weight of the equivalence
						if(weight > maxWeight) {
							maxWeight = weight;
						}
					}
					else {
						loop = true;
					}
				}
			}
		}
		return maxWeight;
	}

	// Find the max weight among all children of an existential expression
	private static int getMaxWeightExist(WeightedAxioms weightedAxioms,
			Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>> e,
			List<Pair<OWLSubClassOfAxiom, Integer>> subsumptions,
			List<Pair<OWLEquivalentClassesAxiom, Integer>> equivalences,
			List<Pair<OWLClassExpression, List<OWLClassExpression>>> classes,
			List<Triplet<OWLClassExpression, List<OWLClassExpression>, List<OWLClassExpression>>> existentialExpressions,
			OWLOntologyManager manager, OWLDataFactory df, Set<OWLSubClassOfAxiom> visitedSubsumptions,
			Set<OWLEquivalentClassesAxiom> visitedEquivalences) {
		
		int maxWeight = -1;
		int weight;

		// Direct children
		// Loop through all children and find max weight among them
		for(OWLClassExpression child : e.getValue1()) {
			// Find matching subsumptions
			OWLSubClassOfAxiom childAxiom = df.getOWLSubClassOfAxiom(child, e.getValue0()); // Create subsumption to check weight of
 
			for(Pair<OWLSubClassOfAxiom, Integer> sub : subsumptions) {		
				if(sub.getValue0().equals(childAxiom)) {
					// Check if axiom has been visit before -- detect looping
					if(!visitedSubsumptions.contains(childAxiom)) {
						visitedSubsumptions.add(childAxiom);
						weight = getSubsumptionWeight(weightedAxioms, sub, classes, existentialExpressions, subsumptions, equivalences, manager, df, visitedSubsumptions, visitedEquivalences); // Get the weight of the subsumption
						if(weight > maxWeight) {
							maxWeight = weight;
						}
					}
					else {
						loop = true;
					}
				}
			}
			
			// Find matching equivalence
			OWLEquivalentClassesAxiom childEquivalence= df.getOWLEquivalentClassesAxiom(child, e.getValue0()); // Create equivalence to check weight of
			
			for(Pair<OWLEquivalentClassesAxiom, Integer> eq : equivalences) {		
				if(eq.getValue0().equals(childEquivalence)) {
					// Check if axiom has been visit before -- detect looping
					if(!visitedEquivalences.contains(childEquivalence)) {
						visitedEquivalences.add(childEquivalence);
						weight = getEquivalenceWeight(weightedAxioms, eq, classes, existentialExpressions, subsumptions, equivalences, manager, df, visitedSubsumptions, visitedEquivalences); // Get the weight of the equivalence
						if(weight > maxWeight) {
							maxWeight = weight;
						}
					}
					else {
						loop = true;
					}
				}
			}
		}

		
		// Indirect children
		for(OWLClassExpression indirectChild : e.getValue2()) {
			// Find subsumptions with the indirect child as the super class
			for(Pair<OWLSubClassOfAxiom, Integer> sub : subsumptions) {
				if(sub.getValue0().getSuperClass().equals(indirectChild)) {
					// Check if axiom has been visit before -- detect looping
					if(!visitedSubsumptions.contains(sub.getValue0())){
						visitedSubsumptions.add(sub.getValue0());
						weight = getSubsumptionWeight(weightedAxioms, sub, classes, existentialExpressions, subsumptions, equivalences, manager, df, visitedSubsumptions, visitedEquivalences); // Get the weight of the subsumption
						if(weight > maxWeight) {
							maxWeight = weight;
						}
					}
				}
			}
			
			// Find matching equivalences
			for(Pair<OWLEquivalentClassesAxiom, Integer> eq : equivalences) {
				if(eq.getValue0().getClassExpressions().contains(indirectChild)) {
					// Check if axiom has been visit before -- detect looping
					if(!visitedEquivalences.contains(eq.getValue0())){
						visitedEquivalences.add(eq.getValue0());
						weight = getEquivalenceWeight(weightedAxioms, eq, classes, existentialExpressions, subsumptions, equivalences, manager, df, visitedSubsumptions, visitedEquivalences); // Get the weight of the subsumption
						if(weight > maxWeight) {
							maxWeight = weight;
						}
					}
				}
			}
		}
		

		return maxWeight;
	}

	// Find the max weight among all children of a role expression
	private static int getMaxRoleWeight(WeightedAxioms weightedAxioms,
			Pair<OWLObjectProperty, List<OWLObjectProperty>> role, 
			List<Pair<OWLObjectProperty,List<OWLObjectProperty>>> roles, 
			List<Pair<OWLSubObjectPropertyOfAxiom, Integer>> roleSubsumptions,
			List<Pair<OWLEquivalentObjectPropertiesAxiom,Integer>> roleEquivalences, 
			OWLOntologyManager manager,
			OWLDataFactory df, 
			Set<OWLSubObjectPropertyOfAxiom> visitedRoleSubsumptions,
			Set<OWLEquivalentObjectPropertiesAxiom> visitedRoleEquivalences) {
		
		int maxWeight = -1;
		int weight;

		// Loop through all children and find max weight among them
		for(OWLObjectProperty child : role.getValue1()) {
			// Find matching subsumptions
			OWLSubObjectPropertyOfAxiom childAxiom = df.getOWLSubObjectPropertyOfAxiom(child, role.getValue0()); // Create subsumption to check weight of
			 
			// Find subsumption in role subsumption list 
			for(Pair<OWLSubObjectPropertyOfAxiom, Integer> sub : roleSubsumptions) {		
				if(sub.getValue0().equals(childAxiom)) {
					// Check if axiom has been visit before -- detect looping
					if(!visitedRoleSubsumptions.contains(childAxiom)) {
						visitedRoleSubsumptions.add(childAxiom);
						weight = getRoleSubsumptionWeight(weightedAxioms, sub, roles, roleSubsumptions, roleEquivalences,
														  manager, df, visitedRoleSubsumptions, visitedRoleEquivalences); // Get the weight of the subsumption
						if(weight > maxWeight) {
							maxWeight = weight;
						}
					}
				}
			}
			
			// Find matching equivalence
			OWLEquivalentObjectPropertiesAxiom childEquivalence= df.getOWLEquivalentObjectPropertiesAxiom(child, role.getValue0()); // Create equivalence to check weight of
			
			// Find equivalence in equivalences list
			for(Pair<OWLEquivalentObjectPropertiesAxiom, Integer> eq : roleEquivalences) {		
				if(eq.getValue0().equals(childEquivalence)) {
					// Check if axiom has been visit before -- detect looping
					if(!visitedRoleEquivalences.contains(childEquivalence)) {
						visitedRoleEquivalences.add(childEquivalence);
						weight = getRoleEquivalenceWeight(weightedAxioms, eq, roles, roleSubsumptions, roleEquivalences,
													      manager, df, visitedRoleSubsumptions, visitedRoleEquivalences); // Get the weight of the equivalence
						if(weight > maxWeight) {
							maxWeight = weight;
						}
					}
				}
			}
		}
		return maxWeight;
	}

	// Find the role's weight within GCI axioms
	private static int getWeightedRoleValue(Pair<Pair<OWLObjectProperty,List<OWLObjectProperty>>,Integer> role, WeightedAxioms weightedAxioms) {
		
		// If no weight assigned
		if(role.getValue1() == -1) {
			int max = -1;
			
			//Subsumptions
			for(Pair<OWLSubClassOfAxiom, Integer> sub : weightedAxioms.getSubsumptions()) {
				// Look for role in all axioms
				for(OWLObjectProperty subRole : sub.getValue0().getObjectPropertiesInSignature()) {
					if(subRole.equals(role.getValue0().getValue0()) && sub.getValue1()>max) {
						max = sub.getValue1();
					}
				}
			}
			
			// Disjuncitons
			for(Pair<OWLDisjointClassesAxiom, Integer> dis : weightedAxioms.getDisjunctions()) {
				// Look for role in all axioms
				for(OWLObjectProperty disRole : dis.getValue0().getObjectPropertiesInSignature()) {
					if(disRole.equals(role.getValue0().getValue0()) && dis.getValue1()>max) {
						max = dis.getValue1();
					}
				}
			}
			
			// Equivalences
			for(Pair<OWLEquivalentClassesAxiom, Integer> eq : weightedAxioms.getEquivalences()) {
				// Look for role in all axioms
				for(OWLObjectProperty eqRole : eq.getValue0().getObjectPropertiesInSignature()) {
					if(eqRole.equals(role.getValue0().getValue0()) && eq.getValue1()>max) {
						max = eq.getValue1();
					}
				}
			}
			
			return max;
		}
		
		else {
			return role.getValue1();
		}
	}


}	
