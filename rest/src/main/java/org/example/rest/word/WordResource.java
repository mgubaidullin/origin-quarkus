package org.example.rest.word;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/api/words")
public class WordResource {

    @Inject
    @Channel("words")
    Emitter<String> words;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> post(@RequestBody final String word) {
        return Uni.createFrom().item(word).onItem().apply(w -> {
            String key = UUID.randomUUID().toString();
            words.send(KafkaRecord.of(key, w));
            return key;
        });
    }
}
