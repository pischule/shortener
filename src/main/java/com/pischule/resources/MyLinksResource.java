package com.pischule.resources;

import com.pischule.services.LinkService;
import io.quarkus.panache.common.Page;
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
        var panachePage = Page.of(pageIndex, pageSize);
        var ownedLinksPortion = linkService.getOwned(panachePage).list();
        var allOwnedLinks = linkService.getOwned(Page.ofSize(Integer.MAX_VALUE));

        return myLinks
                .data("links", ownedLinksPortion)
                .data("previousPage", panachePage.previous().index)
                .data("page", pageIndex)
                .data("nextPage", panachePage.next().index)
                .data("from", pageIndex * pageSize + 1)
                .data("to", pageIndex * pageSize + ownedLinksPortion.size())
                .data("totalCount", allOwnedLinks.count())
                .data("count", ownedLinksPortion.size());
    }
}
