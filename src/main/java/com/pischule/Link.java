package com.pischule;


import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class Link {
    public String id;
    public String url;
    public long visits;

    public Link(String id, String url, long visits) {
        this.id = id;
        this.url = url;
        this.visits = visits;
    }

    public Link() {
    }

    private static Link from(Row row) {
        return new Link(
                row.getString("id"),
                row.getString("url"),
                row.getLong("visits")
        );
    }

    public static Uni<Link> findById(PgPool client, String id) {
        return client.preparedQuery("""
                        select * from link where id = $1
                        """).execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public static Uni<Link> findByIdIncrementingViews(PgPool client, String id) {
        return client.preparedQuery("""
                        update link set visits = visits + 1
                        where id = $1
                        returning *
                        """).execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Link> save(PgPool client) {
        return client.preparedQuery("""
                        insert into link values ($1, $2)
                        returning *
                        """).execute(Tuple.of(id, url))
                .onItem().transform(rs -> rs.iterator().next())
                .onItem().transform(Link::from);
    }
}
