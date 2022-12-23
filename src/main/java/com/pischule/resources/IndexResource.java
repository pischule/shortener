package com.pischule.resources;

import com.pischule.entity.Link;
import com.pischule.util.IdUtil;
import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import org.hibernate.validator.constraints.URL;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import static javax.ws.rs.core.Response.Status.FOUND;

@ApplicationScoped
@Path("/")
public class IndexResource {
    @Inject
    Template index;

    @Inject
    IdUtil idUtil;

    @Inject
    Template error;

    AtomicLong linksCount = new AtomicLong();
    AtomicLong visitsCount = new AtomicLong();

    @Inject
    PgPool client;

    @GET
    @Path("")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index.data("links", linksCount.get())
                .data("visits", visitsCount.get());
    }

    @PostConstruct
    public void init() {
        updateCounts();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Uni<Response> post(@RestForm @NotNull @Valid @NotBlank @URL String url) {
        Link link = new Link(idUtil.generate(), url, 0);
        linksCount.incrementAndGet();
        return link.save(client)
                .onItem().transform(l -> URI.create("/v/" + link.id))
                .onItem().transform(uri -> Response.status(FOUND).location(uri).build());
    }

    @ServerExceptionMapper
    public Response handleValidationError(ValidationException exception) {
        Log.error(exception);
        return Response
                .status(400)
                .entity(error.data("msg", exception.getMessage()).render())
                .header("Content-Type", "text/html")
                .build();
    }

    @Scheduled(every = "1h")
    public void updateCounts() {
        Link.countAll(client)
                .onItem().invoke(linksCount::set)
                .flatMap(l -> Link.sumVisits(client))
                .onItem().invoke(visitsCount::set)
                .subscribe().with((it) -> Log.info("total count updated"));
    }
}
