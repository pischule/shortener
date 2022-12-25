package com.pischule.resources;

import com.pischule.entity.Link;
import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
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

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id:[A-Za-z0-9_-]{8}}")
    @Blocking
    @Transactional
    public Response redirect(@RestPath String id) {
        Log.infof("redirect, id=%s", id);

        Link link = Link.findById(id);
        if (link == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        link.visits += 1;
        URI uri = URI.create(link.url);

        return Response.status(RestResponse.Status.MOVED_PERMANENTLY)
                .location(uri)
                .entity(redirect.data("url", link.url))
                .build();
    }
}
