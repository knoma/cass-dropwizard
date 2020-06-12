package com.knoma.web.resource;

import com.codahale.metrics.annotation.Timed;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.knoma.web.dao.PersonDAO;
import com.knoma.web.pojo.Person;
import org.glassfish.jersey.server.ManagedAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Path("/person")
public class PersonResource {

    @Inject
    private ExecutorService executor;

    @Inject
    private Session session;
    @Inject
    private PersonDAO personDAO;

    private final Logger logger = LoggerFactory.getLogger(PersonResource.class);

    @GET
    @Timed
    @Path("/{id}")
    @ManagedAsync
    @Produces(MediaType.APPLICATION_JSON)
    public void getPerson(@Suspended final AsyncResponse response, @PathParam("id") UUID id) {
        ListenableFuture<Person> future = personDAO.getById(id);

        Futures.addCallback(future, new FutureCallback<Person>() {
            @Override
            public void onSuccess(@Nullable Person result) {
                response.resume(result);
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("getById error: " + t.getMessage(), t);
                response.resume(buildErrorResponse(t));
            }
        }, executor);
    }

    @DELETE
    @Timed
    @Path("/{id}")
    @ManagedAsync
    @Produces(MediaType.APPLICATION_JSON)
    public void removePerson(@Suspended final AsyncResponse response, @PathParam("id") UUID id) {
        ListenableFuture<Boolean> future = personDAO.delete(id);

        Futures.addCallback(future, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@Nullable Boolean result) {
                ListenableFuture<Long> future = personDAO.getCount();

                Futures.addCallback(future, new FutureCallback<>() {
                    @Override
                    public void onSuccess(@Nullable Long result) {
                        response.resume(ImmutableMap.of("count", result));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        logger.error("getCount error: " + t.getMessage(), t);
                        response.resume(buildErrorResponse(t));
                    }
                }, executor);
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("delete error: " + t.getMessage(), t);
                response.resume(buildErrorResponse(t));

            }
        }, executor);
    }

    @GET
    @Timed
    @Path("/all")
    @ManagedAsync
    @Produces(MediaType.APPLICATION_JSON)
    public void getPersons(@Suspended final AsyncResponse response) {
        Futures.addCallback(personDAO.getAll(), new FutureCallback<List<Person>>() {
            @Override
            public void onSuccess(@Nullable List<Person> result) {
                response.resume(result);
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("getAll error: " + t.getMessage(), t);
                response.resume(buildErrorResponse(t));
            }
        }, executor);
    }

    @GET
    @Timed
    @Path("/count")
    @ManagedAsync
    @Produces(MediaType.APPLICATION_JSON)
    public void getPersonCount(@Suspended final AsyncResponse response) {
        Futures.addCallback(personDAO.getCount(), new FutureCallback<>() {
            @Override
            public void onSuccess(@Nullable Long result) {
                response.resume(ImmutableMap.of("count", result));
            }

            @Override
            public void onFailure(Throwable t) {
                response.resume(buildErrorResponse(t));
            }
        }, executor);
    }

    @POST
    @Timed
    @Path("/")
    @ManagedAsync
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON})
    public void addPerson(@Suspended final AsyncResponse response, Person person) {
        ListenableFuture<Boolean> future = personDAO.save(person.getId(), person.getFirstName(), person.getLastName(), person.getEmail());

        Futures.addCallback(future, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@Nullable Boolean result) {
                response.resume(person);
            }

            @Override
            public void onFailure(Throwable t) {
                response.resume(buildErrorResponse(t));
            }
        }, executor);
    }

    private Response buildErrorResponse(Throwable t) {
        Response.Status status = INTERNAL_SERVER_ERROR;
        String reason = status.getReasonPhrase();
        if (t instanceof DriverException) {
            reason = "Internal Database Error";
        }

        ImmutableMap<String, Object> map = ImmutableMap
                .of("error", reason, "code", status.getStatusCode(), "message", t.getMessage());

        return Response.status(status).entity(map).build();
    }
}
