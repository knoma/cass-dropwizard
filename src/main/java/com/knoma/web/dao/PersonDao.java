package com.knoma.web.dao;

import com.knoma.web.pojo.Person;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface PersonDao {

    @SqlQuery("SELECT * FROM public.person WHERE id = :id")
    @RegisterBeanMapper(Person.class)
    Person getById(@Bind("id") Long userId);

    @SqlUpdate("INSERT INTO public.person (id, first_name, last_name, email)  VALUES (:id, :firstname, :lastname, :email)")
    void save(@Bind("id") Long userId, @Bind("firstname") String firstName, @Bind("lastname") String lastName, @Bind("email") String email);

    @SqlUpdate("DELETE FROM public.person WHERE id = :id")
    void delete(@Bind("id") Long userId);

    @SqlQuery("SELECT count(*) FROM public.person;")
    Long getCount();

    @SqlQuery("SELECT * FROM public.person;")
    @RegisterBeanMapper(Person.class)
    List<Person> getAll();
}
