package org.example.rest.sentence;

import com.datastax.driver.core.Row;
import org.apache.camel.builder.RouteBuilder;
import com.datastax.driver.core.Cluster;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CassandraRoute extends RouteBuilder {

    private final String CQL = "select * from origin.sentences where sentence like ?";

    @ConfigProperty(name = "cassandra", defaultValue = "localhost")
    String cassandra;

    @Named("clusterRef")
    Cluster cluster() {
        return Cluster.builder().addContactPoint(cassandra).build();
    }

    @Override
    public void configure() throws Exception {
        from("direct:req")
                .process(e -> e.getIn().setBody(List.of("%" + e.getIn().getBody() + "%")))
                .to("cql:bean:clusterRef/origin?cql=" + CQL)
                .process(e -> {
                    List<Row> rows = e.getIn().getBody(List.class);
                    List list = rows.stream().map(row ->
                            Map.of("key", row.get(0, String.class), "sentence", row.get(1, String.class)))
                            .collect(Collectors.toList());
                    e.getIn().setBody(list);
                });
    }
}
