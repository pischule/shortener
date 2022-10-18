package com.pischule.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

import javax.persistence.Id;

public class Link extends PanacheEntityBase {
    @Id
    public String id;

    public String url;

    public String toString() {
        String var10000 = this.getClass().getSimpleName();
        return var10000 + "<" + this.id + ">";
    }
}
