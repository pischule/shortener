package com.pischule.resources;

import com.pischule.entity.Link;
import com.pischule.services.LinkService;
import io.quarkus.logging.Log;
import io.quarkus.oidc.IdToken;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

import static javax.ws.rs.core.Response.Status.FOUND;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class IndexResource {
    @Inject
    Template index;

    @Inject
    LinkService linkService;

    @Inject
    @IdToken
    JsonWebToken idToken;

    @GET
    @Path("/")
    public TemplateInstance get() {
        return index
                .data("loggedIn", idToken.getSubject())
                .data("error", null)
                .data("url", "");
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Blocking
    @Transactional
    public Response post(@RestForm String url) {
        Link link;
        try {
            link = linkService.saveUrl(url, idToken.getSubject());
            new URI(url);
        } catch (ConstraintViolationException | URISyntaxException e) {
            var body = index
                    .data("loggedIn", idToken.getSubject())
                    .data("error", "Invalid URL")
                    .data("url", url);
            return Response.status(400).entity(body).build();
        }

        var uri = URI.create("/l/" + link.id);
        return Response.status(FOUND).location(uri).build();
    }


    @GET
    @Authenticated
    @Produces(MediaType.TEXT_HTML)
    @Path("/auth/login")
    public Response token(@RestQuery String redirect) {
        return Response.temporaryRedirect(URI.create(redirect)).build();
    }

    @Inject
    Template redirect;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("{id:[A-Za-z0-9_-]{5,8}}")
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
