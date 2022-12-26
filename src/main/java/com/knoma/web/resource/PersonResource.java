package com.knoma.web.resource;

import com.codahale.metrics.annotation.Timed;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.google.common.collect.ImmutableMap;
import com.knoma.web.dao.PersonDAO;
import com.knoma.web.dao.PersonMapper;
import com.knoma.web.dao.PersonMapperBuilder;
import com.knoma.web.pojo.Person;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.server.ManagedAsync;


import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Path("/person")
public class PersonResource {

    private PersonDAO personDAO;

    @Inject
    public PersonResource(CqlSession session) {
        PersonMapper personMapper = new PersonMapperBuilder(session).build();
        this.personDAO = personMapper.personDao(CqlIdentifier.fromCql("cass_drop"));
    }

    @GET
    @Timed
    @ManagedAsync
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getPerson(@Suspended final AsyncResponse response, @PathParam("id") UUID id) throws ExecutionException, InterruptedException {
        personDAO.getById(id).thenAccept((res) -> {
            if (res != null) {
                response.resume(Response.status(Response.Status.OK).entity(res).build());
            } else {
                response.resume(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    @DELETE
    @Timed
    @ManagedAsync
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void removePerson(@Suspended final AsyncResponse response, @PathParam("id") String id) {
        personDAO.delete(UUID.fromString(id));
        personDAO.getCount().thenAccept((res) ->
                response.resume(Response.status(Response.Status.OK).entity(ImmutableMap.of("count", res)).build()));
    }

    @GET
    @Timed
    @ManagedAsync
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public void getPersons(@Suspended final AsyncResponse response) {
        personDAO.getAll().thenAccept((a) ->
                response.resume(Response.status(Response.Status.OK).entity(a.currentPage()).build()));
    }

    @POST
    @Timed
    @ManagedAsync
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON})
    public void addPerson(@Suspended final AsyncResponse response, Person person) {
        personDAO.saveAsync(person).thenAccept(aVoid -> response.resume(Response.status(Response.Status.CREATED).entity(person).build()));
    }

    @GET
    @Timed
    @ManagedAsync
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public void getPersonCount(@Suspended final AsyncResponse response) {
        personDAO.getCount().whenCompleteAsync((res, err) ->
                response.resume(Response.status(Response.Status.OK).entity(ImmutableMap.of("count", res)).build()));
    }
}
