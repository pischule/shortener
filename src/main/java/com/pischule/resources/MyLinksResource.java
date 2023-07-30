package com.pischule.resources;

import com.pischule.services.LinkService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestQuery;

@Blocking
@Produces(MediaType.TEXT_HTML)
@Path("my-links")
public class MyLinksResource {
    @Inject
    Template myLinks;

    @Inject
    LinkService linkService;

    @GET
    @Authenticated
    public TemplateInstance get(@RestQuery("page") @DefaultValue("0") Integer pageIndex) {
        int pageSize = 20;
        var ownedLinksPortion = linkService.getOwned(pageIndex, pageSize);
        var allOwnedLinks = linkService.countOwned();

        return myLinks
                .data("links", ownedLinksPortion)
                .data("previousPage", pageIndex - 1)
                .data("page", pageIndex)
                .data("nextPage", pageIndex + 1)
                .data("from", pageIndex * pageSize + 1)
                .data("to", pageIndex * pageSize + ownedLinksPortion.size())
                .data("totalCount", allOwnedLinks)
                .data("count", ownedLinksPortion.size());
    }
}
