package org.acme.callcenter.persistence.jpa;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.Skill;

@Entity
public class JpaAgent {

    @Id
    private long id;

    @Column
    private String name;

    @Enumerated
    private List<Skill> skills;

    @OneToOne
    private JpaCall nextCall;

    public JpaAgent(long id, String name, List<Skill> skills, JpaCall nextCall) {
        this.id = id;
        this.name = name;
        this.skills = skills;
        this.nextCall = nextCall;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public JpaCall getNextCall() {
        return nextCall;
    }
}
