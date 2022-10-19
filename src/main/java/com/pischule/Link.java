package com.pischule;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class Link extends PanacheEntityBase {
    @Id
    @Column(length = 36)
    public String id;
    @Column(length = 512)
    public String url;
    public long visits;
    public Instant createdAt;
}
