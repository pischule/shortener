package com.pischule.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    @CreationTimestamp
    public ZonedDateTime createdAt;
    @Column
    @UpdateTimestamp
    public ZonedDateTime updatedAt;
    public String creator;
}
