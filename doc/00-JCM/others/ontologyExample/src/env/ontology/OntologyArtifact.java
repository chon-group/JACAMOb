package ontology;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;


public class OntologyArtifact extends Artifact {

    private static final String BASE_IRI = "http://www.example.org/team#";

    private OWLOntologyManager manager;
    private OWLOntology ontology;
    private OWLDataFactory dataFactory;
    private OWLReasoner reasoner;


    public void init(String ontologyFile) {

        try {

            System.out.println(
                "[OntologyArtifact] Loading ontology: " + ontologyFile
            );

            /*
             * Cria o gerenciador OWL.
             */
            manager = OWLManager.createOWLOntologyManager();


            /*
             * Carrega o arquivo .owl.
             */
            File file = new File(ontologyFile);

            ontology =
                manager.loadOntologyFromOntologyDocument(file);


            /*
             * DataFactory é usada para obter referências
             * para classes, indivíduos, propriedades etc.
             */
            dataFactory =
                manager.getOWLDataFactory();


            /*
             * Cria o reasoner.
             *
             * Aqui usamos o StructuralReasoner,
             * que já vem com a OWL API.
             */
            StructuralReasonerFactory reasonerFactory =
                new StructuralReasonerFactory();

            reasoner =
                reasonerFactory.createReasoner(ontology);


            /*
             * Calcula previamente as inferências disponíveis.
             */
            reasoner.precomputeInferences();


            System.out.println(
                "[OntologyArtifact] Ontology loaded successfully."
            );

            System.out.println(
                "[OntologyArtifact] Ontology IRI: "
                + ontology.getOntologyID()
            );


        } catch (Exception e) {

            System.err.println(
                "[OntologyArtifact] Error loading ontology."
            );

            e.printStackTrace();

            failed(
                "Could not load ontology: "
                + ontologyFile
            );
        }
    }


    @OPERATION
    public void isInstanceOf(
            String instance,
            String concept,
            OpFeedbackParam<Boolean> result) {

        /*
         * Cria a referência para o indivíduo:
         *
         * http://www.example.org/team#john
         */
        OWLNamedIndividual individual =
            dataFactory.getOWLNamedIndividual(
                IRI.create(BASE_IRI + instance)
            );


        /*
         * Cria a referência para a classe:
         *
         * http://www.example.org/team#Player
         */
        OWLClass owlClass =
            dataFactory.getOWLClass(
                IRI.create(BASE_IRI + concept)
            );


        /*
         * direct = false
         *
         * Significa que queremos TODOS os tipos do indivíduo,
         * incluindo os tipos inferidos pela hierarquia.
         *
         * Por exemplo:
         *
         * john : RightMidfielder
         *
         * RightMidfielder subClassOf Midfielder
         * Midfielder subClassOf Player
         *
         * Portanto:
         *
         * john : RightMidfielder
         * john : Midfielder
         * john : Player
         */
        boolean belongs =
            reasoner
                .getTypes(individual, false)
                .containsEntity(owlClass);


        System.out.println(
            "[OntologyArtifact] "
            + instance
            + " isInstanceOf "
            + concept
            + " ? "
            + belongs
        );


        result.set(belongs);
    }
}