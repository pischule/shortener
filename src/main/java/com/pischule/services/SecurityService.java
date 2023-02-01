package com.pischule.services;

import io.quarkus.oidc.UserInfo;
import io.smallrye.common.constraint.Nullable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

@ApplicationScoped
@Named("securityService")
public class SecurityService {
    @Inject
    UserInfo userInfo;


    @Inject
    @Nullable
    public String getUserId() {
        return Optional.ofNullable(userInfo)
                .filter(it -> it.getJsonObject() != null)
                .map(it -> it.getLong("id"))
                .map(it -> "github-" + it)
                .orElse(null);
    }
}