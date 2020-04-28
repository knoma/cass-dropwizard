package com.knoma.web.resource;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.knoma.web.dao.PersonDao;
import com.knoma.web.pojo.Person;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/person")
public class PersonResource {

    private final PersonDao personDao;


    public PersonResource(Jdbi jdbi) {
        this.personDao = jdbi.onDemand(PersonDao.class);
    }

    @GET
    @Timed
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Person getPerson(@PathParam("id") Long id) {
        System.out.println(personDao.getCount());
        return personDao.getById(id);
    }

    @DELETE
    @Timed
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> removePerson(@PathParam("id") Long id) {
        personDao.delete(id);
        return ImmutableMap.of("count", personDao.getCount());
    }

    @GET
    @Timed
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> getPersons() {
        return personDao.getAll();
    }

    @GET
    @Timed
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> getPersonCount() {
        return ImmutableMap.of("count", personDao.getCount());
    }

    @POST
    @Timed
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON})
    public Person addPerson(Person person) {
        personDao.save(person.getId(), person.getFirstName(), person.getLastName(), person.getEmail());
        return person;
    }
}
