OWL-EL Belief Contraction: 

Program was developed in Eclipse using Maven on Windows 10. Pom file contains all dependencies.

Input: inputFile outputFile belief method	(ex: input.owl output.owl "A SubClassOf B" -s)
	- inputFile: An OWL ontology file. If not in OWL-EL, the program removes axioms that are not allowed in OWL-EL.
	- belief: What you want contract. Written in Manchester syntax (Section 4.2 of https://www.w3.org/TR/owl2-manchester-syntax/). The entire expression is around quotations.
	- method: The approach used to select the drop set.
		-n: choose any min hitting set
		-s: choose most specific hitting set (based on method in the report)	
		

WORK IN PROGRESS
Incomplete Features:
	- Only does subsumption hierarchy and support axiom steps. No loop or offset adjustment fully implemented.
	- No localization method.
	- No conjunctions in axioms.