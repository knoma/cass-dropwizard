package com.knoma.web.dao;

import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.knoma.web.pojo.Person;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class PersonDAO {

    private Mapper<Person> mapper;
    private Session session;
    protected ExecutorService executor;

    @Inject
    public PersonDAO(Session session, ExecutorService executor) {
        this.mapper = new MappingManager(session).mapper(Person.class);
        this.session = session;
        this.executor = executor;
    }

    public ListenableFuture<List<Person>> getAll() {
        ListenableFuture<Result<Person>> future = mapper.mapAsync(session.executeAsync("SELECT * FROM cass_drop.person"));

        return Futures.transformAsync(future, rs -> {
                    List<Person> all = rs.all();
                    return Futures.immediateFuture(all);
                }
                , executor);
    }

    public ListenableFuture<Long> getCount() {
        ResultSetFuture future = session.executeAsync("SELECT count(*) as count FROM cass_drop.person");

        return Futures.transformAsync(future, rs -> {
                    long count = rs.one().getLong("count");
                    return Futures.immediateFuture(count);
                }
                , executor);
    }

    public ListenableFuture<Person> getById(UUID userId) {
        ListenableFuture<Result<Person>> future = mapper.mapAsync(session.executeAsync(String.format("SELECT * FROM cass_drop.person WHERE id = %s", userId)));

        return Futures.transformAsync(future, rs -> {
                    Person one = rs.one();
                    return Futures.immediateFuture(one);
                }
                , executor);
    }

    public ListenableFuture<Boolean> save(UUID userId, String firstName, String lastName, String email) {
        ResultSetFuture future = session.executeAsync(String.format("INSERT INTO cass_drop.person (id, email, first_name, last_name) " +
                " VALUES (%s, '%s','%s', '%s')", userId, email, firstName, lastName));

        return Futures.transformAsync(future, rs -> {
                    boolean applied = rs.wasApplied();
                    return Futures.immediateFuture(applied);
                }
                , executor);
    }

    public ListenableFuture<Boolean> delete(UUID userId) {
        ResultSetFuture future = session.executeAsync(String.format("DELETE FROM cass_drop.person WHERE id = %s", userId));

        return Futures.transformAsync(future, rs -> {
                    boolean applied = rs.wasApplied();
                    return Futures.immediateFuture(applied);
                }
                , executor);
    }
}
