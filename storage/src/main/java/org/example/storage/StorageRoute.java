package org.example.storage;

import com.datastax.driver.core.Cluster;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.KafkaManualCommit;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.List;

@ApplicationScoped
public class StorageRoute extends EndpointRouteBuilder {

    private final String CQL = "insert into sentences(key, sentence) values (?, ?)";

    @ConfigProperty(name = "cassandra", defaultValue = "localhost")
    String cassandra;

    @Named("clusterRef")
    Cluster cluster() {
        return Cluster.builder().addContactPoint(cassandra).build();
    }

    public void configure() throws Exception {

        errorHandler(deadLetterChannel(kafka("dead-letter-queue").getUri()).logExhausted(true)
                .useOriginalMessage().maximumRedeliveries(5).redeliveryDelay(1000));

        from(kafka("sentences").autoCommitEnable(false).allowManualCommit(true))
                .log("Storing sentence with key ${headers.kafka.KEY}")
                .process(exchange -> {
                    String key = exchange.getIn().getHeader("kafka.KEY", String.class);
                    String sentence = exchange.getIn().getBody(String.class);
                    exchange.getIn().setBody(List.of(key, sentence));
                })
                .to("cql:bean:clusterRef/origin?cql=" + CQL)
                .process(exchange -> {
                    KafkaManualCommit manual = exchange.getIn().getHeader(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);
                    manual.commitSync();
                })
                .log("Stored sentence with key ${headers.kafka.KEY}");
    }
}
