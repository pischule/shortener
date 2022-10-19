package com.pischule;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
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

    @PrePersist
    public void prePersist() {
        id = NanoIdUtils.randomNanoId().substring(0, 8);
        createdAt = Instant.now();
    }
}
