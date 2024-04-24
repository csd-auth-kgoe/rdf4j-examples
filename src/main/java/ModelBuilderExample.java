import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public class ModelBuilderExample {
    /**
     * Simple example, showing the use of the ModelBuilder class for defining triples. The model builder class can be
     * used as an alternative way to the model API (SimpleModel.java). It provides a fluent API to quickly and
     * efficiently create RDF model programmatically
     */
    public static void main(String[] args) {
        // using the ModelBuilder, instead of the model api (SimpleModel.java)
        ModelBuilder builder = new ModelBuilder();
        Model model = builder.setNamespace("ex", "http://example.org/")
                .subject("ex:Picasso")
                .add(RDF.TYPE, "ex:Artist")
                .add(FOAF.FIRST_NAME, "Pablo")
                .add("ex:age", 99)
                .build();
        builder.subject("ex:George")
                .add(RDF.TYPE, "ex:Artist")
                .add(FOAF.FIRST_NAME, "George")
                .add("ex:age", 40)
                .build();
        for (Statement statement : model) {
            System.out.println(statement);
        }

    }
}
