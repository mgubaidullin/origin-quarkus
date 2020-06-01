package org.example.rest.sentence;

import io.smallrye.mutiny.Uni;
import org.apache.camel.ProducerTemplate;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/sentences")
public class SentenceResource {

    @Inject
    ProducerTemplate producer;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List> get(@QueryParam("sentence") String sentence) {
        return Uni.createFrom()
                .completionStage(producer.asyncRequestBody("direct:req", sentence, List.class))
                .onItem().apply(list -> list);
    }
}
