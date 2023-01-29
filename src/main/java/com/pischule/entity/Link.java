package com.pischule.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Entity
public class Link extends PanacheEntityBase {
    @Id
    public String id;
    public String url;
    public long visits;
    @Column(insertable = false)
    public ZonedDateTime createdAt;
    public String creator;
}
