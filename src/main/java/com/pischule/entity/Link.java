package com.pischule.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Link extends PanacheEntityBase {
    @Id
    public String id;
    public String url;
    public long visits;
}
