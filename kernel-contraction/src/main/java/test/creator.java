package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;





public class creator {
	public static void main(String[] args) throws OWLOntologyStorageException,
	OWLOntologyCreationException, IOException {
		LogManager.getLogger("org.semanticweb.elk").setLevel(Level.ERROR); // Disable reasoner logging messages
		
		File output = File.createTempFile("test", "owl");
		IRI docIRI = IRI.create(output);
	
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = m.getOWLDataFactory();
		
	
		OWLOntology ont = m.createOntology(docIRI);
		
		/*
		OWLClass cA = df.getOWLClass(IRI.create(docIRI + "#A"));
		OWLClass cA2 = df.getOWLClass(IRI.create(docIRI + "#A2"));
		OWLClass cB = df.getOWLClass(IRI.create(docIRI + "#B"));
		OWLClass cC = df.getOWLClass(IRI.create(docIRI + "#C"));
		OWLClass cD = df.getOWLClass(IRI.create(docIRI + "#D"));
		OWLClass cE = df.getOWLClass(IRI.create(docIRI + "#E"));
		
		

		OWLObjectProperty pR = df.getOWLObjectProperty(IRI.create(docIRI + "#r"));
		OWLObjectProperty pS = df.getOWLObjectProperty(IRI.create(docIRI + "#s"));
		
		
		//OWLAxiom rA_A = df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(pR, cA), cA);
		OWLAxiom A2_A = df.getOWLSubClassOfAxiom(cA2, cA);
		OWLAxiom A_sB = df.getOWLSubClassOfAxiom(cA, df.getOWLObjectSomeValuesFrom(pS, cB));
		OWLAxiom sB_B = df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(pS, cB), cB);
		//OWLAxiom rA_C = df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(pR, cA), cC);
		OWLAxiom A2_C = df.getOWLSubClassOfAxiom(cA2, cC);
		OWLAxiom C_B = df.getOWLSubClassOfAxiom(cC, cB);
		OWLAxiom B_sD = df.getOWLSubClassOfAxiom(cB, df.getOWLObjectSomeValuesFrom(pS, cD));
		OWLAxiom B_dis_E = df.getOWLDisjointClassesAxiom(cB, cE);
		
		//m.addAxiom(ont, rA_A);
		m.addAxiom(ont, A2_A);
		m.addAxiom(ont, A_sB);
		m.addAxiom(ont, sB_B);
		//m.addAxiom(ont, rA_C);
		m.addAxiom(ont, A2_C);
		m.addAxiom(ont, C_B);
		m.addAxiom(ont, B_sD);
		m.addAxiom(ont, B_dis_E);
		
		*/
		
		OWLClass cA = df.getOWLClass(IRI.create(docIRI + "#A"));
		OWLClass cB = df.getOWLClass(IRI.create(docIRI + "#B"));
		OWLClass cC = df.getOWLClass(IRI.create(docIRI + "#C"));
		OWLClass cD = df.getOWLClass(IRI.create(docIRI + "#D"));
		OWLClass cE = df.getOWLClass(IRI.create(docIRI + "#E"));
		OWLClass cF = df.getOWLClass(IRI.create(docIRI + "#F"));
		OWLClass cG = df.getOWLClass(IRI.create(docIRI + "#G"));
		
		
		OWLObjectProperty pR = df.getOWLObjectProperty(IRI.create(docIRI + "#r"));
		OWLObjectProperty pS = df.getOWLObjectProperty(IRI.create(docIRI + "#s"));
		OWLObjectProperty pQ = df.getOWLObjectProperty(IRI.create(docIRI + "#q"));
		OWLObjectProperty pP = df.getOWLObjectProperty(IRI.create(docIRI + "#p"));
		OWLObjectProperty pT = df.getOWLObjectProperty(IRI.create(docIRI + "#t"));
		OWLObjectProperty pU = df.getOWLObjectProperty(IRI.create(docIRI + "#u"));
		OWLObjectProperty pV = df.getOWLObjectProperty(IRI.create(docIRI + "#v"));
		OWLObjectProperty pW = df.getOWLObjectProperty(IRI.create(docIRI + "#w"));
		
		
		
		
		OWLAxiom A_B = df.getOWLSubClassOfAxiom(cA, cB);
		OWLAxiom B_eq_rC = df.getOWLEquivalentClassesAxiom(cB, df.getOWLObjectSomeValuesFrom(pR, cC));
		OWLAxiom rC_eq_D = df.getOWLEquivalentClassesAxiom(df.getOWLObjectSomeValuesFrom(pR, cC), cD);
		OWLAxiom rC__D = df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(pR, cC), cD);
		OWLAxiom D_E = df.getOWLSubClassOfAxiom(cD, cE);
		OWLAxiom A_eq_F = df.getOWLEquivalentClassesAxiom(cA, cF);
		OWLAxiom F_rD = df.getOWLSubClassOfAxiom(cF, df.getOWLObjectSomeValuesFrom(pR, cD));
		OWLAxiom A_rC = df.getOWLSubClassOfAxiom(cA, df.getOWLObjectSomeValuesFrom(pR, cC));
		OWLAxiom rD_G = df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(pR, cD), cG);
		OWLAxiom G_eq_E = df.getOWLEquivalentClassesAxiom(cG, cE);
		OWLAxiom B_sC = df.getOWLSubClassOfAxiom(cB, df.getOWLObjectSomeValuesFrom(pS, cC));
		OWLAxiom s_r = df.getOWLSubObjectPropertyOfAxiom(pS, pR);
		OWLAxiom r_q = df.getOWLSubObjectPropertyOfAxiom(pR, pQ);
		OWLAxiom A_eq_B = df.getOWLEquivalentClassesAxiom(cA, cB);
		OWLAxiom B_eq_C = df.getOWLEquivalentClassesAxiom(cB, cC);
		OWLAxiom C_eq_D = df.getOWLEquivalentClassesAxiom(cC, cD);
		OWLAxiom qC__D = df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(pQ, cC), cD);

		OWLAxiom r_s = df.getOWLSubObjectPropertyOfAxiom(pR, pS);
		OWLAxiom  s_eq_q = df.getOWLEquivalentObjectPropertiesAxiom(pS, pQ);
		OWLAxiom  q_eq_p = df.getOWLEquivalentObjectPropertiesAxiom(pQ, pP);
		OWLAxiom p_t = df.getOWLSubObjectPropertyOfAxiom(pP, pT);
		OWLAxiom  r_eq_u = df.getOWLEquivalentObjectPropertiesAxiom(pR, pU);
		OWLAxiom u_v = df.getOWLSubObjectPropertyOfAxiom(pU, pV);
		OWLAxiom v_w = df.getOWLSubObjectPropertyOfAxiom(pV, pW);
		OWLAxiom  w_eq_T = df.getOWLEquivalentObjectPropertiesAxiom(pW, pT);
		
		
		OWLAxiom a = df.getOWLSubClassOfAxiom(cA, cB);
		OWLAxiom b = df.getOWLEquivalentClassesAxiom(cB, cC);
		OWLAxiom d = df.getOWLSubClassOfAxiom(cC, df.getOWLObjectSomeValuesFrom(pR, cD));
		OWLAxiom f = df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(pS, cD), cE);
		
		OWLAxiom g = df.getOWLSubObjectPropertyOfAxiom(pR, pS);
		OWLAxiom h = df.getOWLSubObjectPropertyOfAxiom(pS, pP);
		OWLAxiom i = df.getOWLSubObjectPropertyOfAxiom(pP, pQ);
		
		//OWLAxiom g = df.getOWLSubClassOfAxiom(cF, cG);
		//OWLAxiom h = df.getOWLSubClassOfAxiom(cC, cG);
		
		
		
	
		
		
		/*
		OWLAxiom b = df.getOWLEquivalentObjectPropertiesAxiom(pS, pR);
		OWLAxiom c = df.getOWLDisjointClassesAxiom(df.getOWLObjectSomeValuesFrom(pR, cC), cD);
		OWLAxiom cc = df.getOWLDisjointClassesAxiom(df.getOWLObjectSomeValuesFrom(pR, cD),cE);
		OWLAxiom ccc = df.getOWLEquivalentClassesAxiom(cB, cC);
		OWLAxiom e = df.getOWLSubClassOfAxiom(cC, cG);
		OWLAxiom f = df.getOWLSubClassOfAxiom(cG, cD);
		OWLAxiom g = df.getOWLEquivalentClassesAxiom(df.getOWLObjectSomeValuesFrom(pR, cG), df.getOWLObjectSomeValuesFrom(pS, cF));
		OWLAxiom d = df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(pR, cD), cE);
		OWLAxiom h = df.getOWLDisjointClassesAxiom(cE, df.getOWLObjectSomeValuesFrom(pR, cD));
		*/
		OWLSubClassOfAxiom oo = df.getOWLSubClassOfAxiom(df.getOWLObjectUnionOf(cA, cB), cC);
		//System.out.println(oo.getSubClass());
		
		m.addAxiom(ont, a);
		m.addAxiom(ont, b);
		m.addAxiom(ont, d);
		m.addAxiom(ont, f);
		m.addAxiom(ont, g);
		m.addAxiom(ont, h);
		m.addAxiom(ont, i);


		
		OWLOntologyManager outputOntologyManager = OWLManager.createOWLOntologyManager();
		
		OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(ont);
		
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

		// To generate an inferred ontology we use implementations of
		// inferred axiom generators
		List<InferredAxiomGenerator<? extends OWLAxiom>> gens = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
		gens.add(new InferredSubClassAxiomGenerator());
		gens.add(new InferredEquivalentClassAxiomGenerator());
		//gens.add(new InferredSubObjectPropertyAxiomGenerator());
		//gens.add(new InferredEquivalentObjectPropertyAxiomGenerator());
		
		

		// Put the inferred axioms into a fresh empty ontology.
		OWLOntology infOnt = outputOntologyManager.createOntology();
		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner,
				gens);
		//iog.fillOntology(outputOntologyManager, infOnt);
		
		//System.out.println(infOnt.getAxioms());
		
		ExplanationGeneratorFactory<OWLAxiom> explanationFactory = ExplanationManager.createExplanationGeneratorFactory(reasonerFactory);
		ExplanationGenerator<OWLAxiom> explanationGenerator = explanationFactory.createExplanationGenerator(ont);
		
		OWLAxiom belief = df.getOWLSubClassOfAxiom(cB, df.getOWLObjectSomeValuesFrom(pR, cD));
		
		Set<Explanation<OWLAxiom>> kernels = explanationGenerator.getExplanations(belief);
		
		//System.out.println(kernels);
		
		//System.out.println(kernels.iterator().next().contains());
		
		for (OWLAxiom x : ont.getAxioms()) {
			System.out.println(x);
		}
		
		
		
		
	
		
		
	m.saveOntology(ont, new FileOutputStream("C:\\Users\\Ben\\Desktop\\ontologies\\fixing-test.owl"));

		
		
		
		
	}
}
