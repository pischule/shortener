package com.pischule.resources;

import com.pischule.entity.Link;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestPath;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v")
public class LinkResource {
    @Inject
    PgPool client;

    @Inject
    Template view;

    @Inject
    Template error;

    @ConfigProperty(name = "base-url")
    String baseUrl;


    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id:[A-Za-z0-9_-]{8}}")
    public Uni<TemplateInstance> get(@RestPath String id) {
        return Link.findById(client, id)
                .onItem().ifNotNull().transform(link -> view
                        .data("link", link)
                        .data("baseUrl", baseUrl))
                .onItem().ifNull().continueWith(
                        () -> error.data("msg", "Not found"));
    }
}
