package com.pischule.resources;

import com.pischule.entity.Link;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestPath;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v")
@Blocking
public class LinkResource {

    @Inject
    Template view;

    @Inject
    Template error;

    @ConfigProperty(name = "base-url")
    String baseUrl;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id:[A-Za-z0-9_-]{5,8}}")
    public TemplateInstance get(@RestPath String id) {
        Link link = Link.findById(id);
        if (link == null) {
            return error.data("msg", "Link not found");
        }

        return view.data("link", link)
                .data("baseUrl", baseUrl);
    }
}
