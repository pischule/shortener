package com.pischule.resources;

import com.pischule.entity.Link;
import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@ApplicationScoped
@Path("/")
public class RedirectsResource {
    @Inject
    Template redirect;

    @Inject
    PgPool client;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id:[A-Za-z0-9_-]{8}}")
    public Uni<Response> redirect(@RestPath String id) {
        Log.infof("redirect, id=%s", id);

        return Link.findByIdIncrementingViews(client, id)
                .onItem().ifNotNull().transform(link -> {
                    String url = link.url;
                    URI uri = URI.create(url);
                    return Response.status(RestResponse.Status.MOVED_PERMANENTLY)
                            .location(uri)
                            .entity(redirect.data("url", url))
                            .build();
                })
                .onItem().ifNull().continueWith(
                        () -> Response.status(Response.Status.NOT_FOUND).build());
    }
}
