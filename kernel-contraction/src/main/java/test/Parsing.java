package test;

import java.io.File;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.io.OWLOntologyInputSourceException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

public class Parsing {

	public static OWLAxiom parseBelief(String[] args, OWLOntology ontology, OWLOntologyManager manager, OWLDataFactory df) throws ParserException {
		Set<OWLOntology> importsClosure = ontology.getImportsClosure();
		OWLEntityChecker entityChecker = new ShortFormEntityChecker(
				 					new BidirectionalShortFormProviderAdapter(manager, importsClosure, 
				 					new SimpleShortFormProvider()));
		ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
		BidirectionalShortFormProvider biShortFormProvider = new BidirectionalShortFormProviderAdapter(manager, importsClosure, shortFormProvider);
		
		String beliefString = args[2];
		
		
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(
                df, beliefString);
		parser.setDefaultOntology(ontology);
		parser.setOWLEntityChecker(entityChecker);
		
		// Parse inputed belief
		OWLAxiom belief = null;
		try {
			belief = parser.parseAxiom();
		}
		catch (ParserException e) {
			System.out.println("Invalid belief syntax at '" + e.getCurrentToken() + "'");
			System.exit(0);
		}
		
		return belief;	
	}
	
	public static void checkInputCount(String[] args) {
		if(args.length < 3 || args.length > 4) {
			System.out.println("Incorrect number of arguments");
			System.exit(0);
		}
	}
	
	public static int parseHeuristic(String args[]) {
		// Default to specificity when no input
		if(args.length != 4) {
			return 1;
		}
		
		// Specificity
		else if(args[3].equals("-s")) {
			return 1;
		}
		
		// Localization
		else if(args[3].equals("-l")) {
			return 2;
		}
		
		// None
		else if(args[3].equals("-n")) {
			return 3;
		}
		
		// Invalid input
		else {
			System.out.println("Invalid heuristic option '" + args[3] + "'");
			System.exit(0);
		}
		return 0;
	}
	
	public static OWLOntology loadOntology(OWLOntologyManager ontologyManager, String[] args) throws OWLOntologyCreationException {
		OWLOntology ontology = null;
		try {
			ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(args[0]));
		} 
		catch (NoSuchMethodError e){
			System.out.println("Error while parsing ontology. Incorrect OWL formatting.");
			System.exit(0);
		}
		catch (OWLOntologyInputSourceException e) {
			System.out.println("Error while loading ontology. File does not exist");
			System.exit(0);
		}
		
		return ontology;
	}
	
	
}		
		

