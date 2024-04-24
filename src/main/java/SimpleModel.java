import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class SimpleModel {

    /**
     * Simple example, showing: 1. The use of the Model API to define triples 2. The way to save a model in a file 3.
     * The definition and execution of a sparql query
     */
    public static void main(String[] args) throws IOException {

        // Create a new, empty Model object.
        Model model = new TreeModel();


        // We want to reuse this namespace when creating several building blocks.
        String ex = "http://example.org/";

        // We can add also the namespace to the model
        model.setNamespace("ex", ex);

        // Create IRIs for the resources we want to add.
        IRI picasso = Values.iri(ex, "Picasso");
        IRI george = Values.iri(ex, "George");
        IRI artist = Values.iri(ex, "Artist");
        IRI age = Values.iri(ex, "age");

        // add our first statement: Picasso is an Artist
        model.add(picasso, RDF.TYPE, artist);
        model.add(george, RDF.TYPE, artist);

        // second statement: Picasso's first name is "Pablo".
        model.add(picasso, FOAF.FIRST_NAME, Values.literal("Pablo"));
        model.add(george, FOAF.FIRST_NAME, Values.literal("George"));

        // one more statement about the age..
        model.add(picasso, age, Values.literal(99));
        model.add(george, age, Values.literal(40));

        // iterate over the model and print the statements
        System.out.println("iterate over the model and print the statements");
        for (Statement statement : model) {
            System.out.println(statement);
        }

        System.out.println("-----------------------------------------------------------------------");

        // You can also use write to print the model
        System.out.println("You can also use write to print the model");
        Rio.write(model, System.out, RDFFormat.TURTLE);

        System.out.println("-----------------------------------------------------------------------");

        //System.exit(1);

        // Note that instead of writing to the screen using `System.out` you could also provide
        // a java.io.FileOutputStream or a java.io.FileWriter to save the model to a file
        Writer output = new FileWriter("./src/main/resources/rdf_examples/example1.ttl");
        Rio.write(model, output, RDFFormat.TURTLE);

        //
        System.out.println("Print the subjects, predicates and objects");
        Model filter = model.filter(null, null, null);
        for (Statement statement : filter) {
            IRI subject = (IRI) statement.getSubject();
            IRI predicate = statement.getPredicate();
            // the property value could be an IRI, a BNode, a Literal, or an RDF-star Triple. In RDF4J, Value
            // is the supertype of all possible kinds of RDF values.
            Value object = statement.getObject();
            System.out.println(subject);
            System.out.println(predicate);
            System.out.println(object);
        }

        System.out.println("-----------------------------------------------------------------------");

        System.out.println("Print the predicates and objects of 'picasso' subjects");
        filter = model.filter(picasso, null, null);
        for (Statement statement : filter) {
            System.out.println(statement);
        }

        System.out.println("-----------------------------------------------------------------------");


        System.out.println("SPARQL query");
        // We do a simple SPARQL SELECT-query that retrieves all resources of type `ex:Artist`,
        // and their first names.
        String queryString = "PREFIX ex: <http://example.org/> \n";
        queryString += "PREFIX foaf: <" + FOAF.NAMESPACE + "> \n";
        queryString += "SELECT ?s ?n \n";
        queryString += "WHERE { \n";
        queryString += "    ?s a ex:Artist; \n";
        queryString += "       foaf:firstName ?n .";
        queryString += "}";

        System.out.println(queryString);

        System.out.println("-----------------------------------------------------------------------");

        // in memory repository
        Repository repo = new SailRepository(new MemoryStore());
        // adding the model

        System.out.println("Print SPARQL query results");
        // Open a connection to the database
        try (RepositoryConnection conn = repo.getConnection()) {
            // add the model
            conn.add(model);
            TupleQuery query = conn.prepareTupleQuery(queryString);

            // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
            try (TupleQueryResult result = query.evaluate()) {
                // we just iterate over all solutions in the result...
                for (BindingSet solution : result) {
                    // ... and print out the value of the variable binding for ?s and ?n
                    System.out.println("?s = " + solution.getValue("s"));
                    System.out.println("?n = " + solution.getValue("n"));
                }
            }
        } finally {
            // before our program exits, make sure the database is properly shut down.
            repo.shutDown();
        }
    }
}
