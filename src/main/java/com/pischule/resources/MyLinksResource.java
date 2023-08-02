package com.pischule.resources;

import com.pischule.model.Link;
import com.pischule.model.Page;
import com.pischule.services.LinkService;
import io.quarkus.qute.CheckedTemplate;
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
    LinkService linkService;

    @GET
    @Authenticated
    public TemplateInstance get(@RestQuery("page") @DefaultValue("0") Integer pageIndex) {
        int pageSize = 20;
        var page = linkService.getOwned(pageIndex, pageSize);
        return Templates.myLinks(page);
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance myLinks(Page<Link> page);
    }
}
