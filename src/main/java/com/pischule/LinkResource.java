package com.pischule;

import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.validator.constraints.URL;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse.Status;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.annotation.PostConstruct;
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

@Path("/")
public class LinkResource {
    @Inject
    PgPool client;

    @Inject
    Template index;

    @Inject
    Template view;

    @Inject
    Template error;

    @Inject
    Template redirect;

    @Inject
    IdUtil idUtil;

    @ConfigProperty(name = "base-url")
    String baseUrl;

    AtomicLong linksCount = new AtomicLong();
    AtomicLong visitsCount = new AtomicLong();

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

    @Scheduled(every = "1h")
    public void updateCounts() {
        Link.countAll(client)
                .onItem().invoke(linksCount::set)
                .flatMap(l -> Link.sumVisits(client))
                .onItem().invoke(visitsCount::set)
                .subscribe().with((it) -> Log.info("total count updated"));
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

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/v/{id:[A-Za-z0-9_-]{8}}")
    public Uni<TemplateInstance> view(@RestPath String id) {
        return Link.findById(client, id)
                .onItem().ifNotNull().transform(link -> view
                        .data("link", link)
                        .data("baseUrl", baseUrl))
                .onItem().ifNull().continueWith(
                        () -> error.data("msg", "Not found"));
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id:[A-Za-z0-9_-]{8}}")
    public Uni<Response> redirect(@RestPath String id) {
        Log.infof("redirect, id=%s", id);

        visitsCount.incrementAndGet();

        return Link.findByIdIncrementingViews(client, id)
                .onItem().ifNotNull().transform(link -> {
                    String url = link.url;
                    URI uri = URI.create(url);
                    return Response.status(Status.MOVED_PERMANENTLY)
                            .location(uri)
                            .entity(redirect.data("url", url))
                            .build();
                })
                .onItem().ifNull().continueWith(
                        () -> Response.status(Response.Status.NOT_FOUND).build());
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

}
