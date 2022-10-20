package com.pischule;

import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

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
    IdUtil idUtil;

    @GET
    @Path("")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index.instance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Uni<Response> post(@RestForm String url) {
        Link link = new Link();
        link.url = url;
        link.id = idUtil.generate();

        return link.save(client)
                .onItem().transform(l -> URI.create("/v/" + link.id))
                .onItem().transform(uri -> Response.seeOther(uri).build());
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/v/{id:[A-Za-z0-9_-]{8}}")
    public Uni<TemplateInstance> view(@Context UriInfo uriInfo, @RestPath String id) {
        return Link.findById(client, id)
                .onItem().ifNotNull().transform(link -> view
                        .data("link", link.id)
                        .data("visits", link.visits)
                        .data("absoluteLink", uriInfo.getBaseUri() + link.id))
                .onItem().ifNull().continueWith(
                        () -> error.data("msg", "Not found"));
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id:[A-Za-z0-9_-]{8}}")
    public Uni<Response> redirect(@RestPath String id) {
        Log.infof("redirect, id=%s", id);
        return Link.findByIdIncrementingViews(client, id)
                .onItem().ifNotNull().transform(link -> {
                    String url = link.url;
                    URI uri = URI.create(url);
                    return Response.temporaryRedirect(uri).build();
                })
                .onItem().ifNull().continueWith(
                        () -> Response.status(Response.Status.NOT_FOUND).build());
    }
}
