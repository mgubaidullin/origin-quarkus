package org.example.aggregator;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class AggregatorRoute extends EndpointRouteBuilder {

    @ConfigProperty(name = "interval")
    int interval;

    public void configure() throws Exception {

        errorHandler(deadLetterChannel(kafka("dead-letter-queue").getUri())
                .log("${headers}")
                .log("${body}")
                .useOriginalMessage().maximumRedeliveries(5).redeliveryDelay(1000));

        from(kafka("words"))
                .log("Aggregating word with key ${headers.kafka.KEY}")
                .aggregate(constant("all"), (oldExchange, newExchange) -> {
                    if (oldExchange == null) {
                        return newExchange;
                    }
                    StringBuilder sentence = new StringBuilder(oldExchange.getIn().getBody(String.class))
                            .append(" ").append(newExchange.getIn().getBody(String.class));
                    newExchange.getIn().setBody(sentence.toString());
                    newExchange.getIn().setHeader("kafka.KEY", UUID.randomUUID().toString());
                    return newExchange;
                })
                .completionInterval(interval)
                .log("Publishing sentences with key ${headers.kafka.KEY}")
                .to(kafka("sentences").lazyStartProducer(true));
    }
}
