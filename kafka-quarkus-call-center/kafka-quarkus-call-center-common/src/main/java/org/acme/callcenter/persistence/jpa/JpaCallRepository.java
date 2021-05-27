package org.acme.callcenter.persistence.jpa;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class JpaCallRepository implements PanacheRepository<JpaCall> {
}
