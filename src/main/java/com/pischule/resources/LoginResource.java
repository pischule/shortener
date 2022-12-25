package com.pischule.resources;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/login")
public class LoginResource {

    @Inject
    Template login;

    @GET
    public TemplateInstance get() {
        return login.instance();
    }
}
