import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.IOException;

public class GraphDBExample {
    public static void main(String[] args) {

        HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/testrepo");
        RepositoryConnection connection = repository.getConnection();

        // load a simple ontology from a file
        connection.begin();
        // Adding the family ontology
        try {
            connection.add(GraphDBExample.class.getResourceAsStream("/simple_ontology.ttl"), "urn:base",
                    RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Committing the transaction persists the data
        connection.commit();

        ModelBuilder builder = new ModelBuilder();
        builder.setNamespace("ex", "http://www.semanticweb.org/simple_ontology#")
                .subject("ex:george")
                .add(RDF.TYPE, "ex:Professor");

        Model model = builder.build();

        // add our data
        connection.begin();
        connection.add(model);
        connection.commit();

        // We do a simple SPARQL SELECT-query that retrieves all resources of type `ex:Artist`,
        // and their first names.
        String queryString = "PREFIX ex: <http://www.semanticweb.org/simple_ontology#> \n";
        queryString += "SELECT ?p \n";
        queryString += "WHERE { \n";
        queryString += "    ?p a ex:Person. \n";
        queryString += "}";

        TupleQuery query = connection.prepareTupleQuery(queryString);

        // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
        try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
            for (BindingSet solution : result) {
                // ... and print out the value of the variable binding for ?s and ?n
                System.out.println("?p = " + solution.getValue("p"));
            }
        }

        connection.close();
        repository.shutDown();
    }
}
