import be.ugent.rml.Executor;
import be.ugent.rml.Utils;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.IDLabFunctions;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RMLPipeline {

    /**
     * Using the RMLMapper together with the graphdb: perform a mapping (airports) and load the results in graphdb
     */
    public static void main(String[] args) throws IOException {
        RMLPipeline pipeline = new RMLPipeline();

        String rootFolder = "./src/main/resources/rml/";
        String templateFile = "airports/airport-codes-small.mappings_1.ttl";
        String outputFile = "airports/outputFile.ttl";

        String mapPath = rootFolder + templateFile;
        File mappingFile = new File(mapPath);

        String outPath = rootFolder + outputFile;
        Writer output = new FileWriter(outPath);

        // run the RML Mapper
        pipeline.runRMLMapper(mappingFile, output);

        //load data to graphdb
        //domain ontology

        HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/rmlpipeline");
        RepositoryConnection connection = repository.getConnection();

        // Clear the repository before we start
        connection.clear();

        // load a simple ontology from a file
        connection.begin();
        // Adding the family ontology
        try {
            connection.add(RMLPipeline.class.getResourceAsStream("/rml/airports/Airports.owl"),
                    "urn:base",
                    RDFFormat.TURTLE);
            connection.add(GraphDBExample.class.getResourceAsStream("/rml/airports/outputFile.ttl"),
                    "urn:base",
                    RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Committing the transaction persists the data
        connection.commit();

        repository.shutDown();

    }


    void runRMLMapper(File mappingFile, Writer outputFile) {
        try {
            // Get the mapping string stream
            InputStream mappingStream = new FileInputStream(mappingFile);

            // Load the mapping in a QuadStore
            QuadStore rmlStore = QuadStoreFactory.read(mappingStream);

            // Set up the basepath for the records factory, i.e., the basepath for the (local file) data sources
            RecordsFactory factory = new RecordsFactory(mappingFile.getParent());

            // Set up the functions used during the mapping
            Map<String, Class> libraryMap = new HashMap<>();
            libraryMap.put("IDLabFunctions", IDLabFunctions.class);

            FunctionLoader functionLoader = new FunctionLoader(null, libraryMap);

            // Set up the outputstore (needed when you want to output something else than nquads
            QuadStore outputStore = new RDF4JStore();

            // Create the Executor
            Executor executor = new Executor(rmlStore, factory, functionLoader, outputStore, Utils.getBaseDirectiveTurtle(mappingStream));

            // Execute the mapping
            QuadStore result = executor.executeV5(null).get(new NamedNode("rmlmapper://default.store"));

            // Output the result in console
            //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
            //result.write(out, "turtle");
            //out.close();

            // Output the results in a file
            result.write(outputFile, "turtle");
            outputFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
