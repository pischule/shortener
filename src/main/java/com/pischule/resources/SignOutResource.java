package com.pischule.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.core.Cookie.DEFAULT_VERSION;

@Path("sign-out")
public class SignOutResource {
    @GET
    public Response get() {
        return Response.seeOther(URI.create("/"))
                .cookie(new NewCookie("q_session", "", null, null, DEFAULT_VERSION, null, 0, null, false, false))
                .build();
    }
}
