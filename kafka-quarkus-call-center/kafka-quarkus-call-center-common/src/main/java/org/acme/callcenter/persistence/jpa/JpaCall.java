package org.acme.callcenter.persistence.jpa;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.acme.callcenter.domain.Skill;

@Entity
public class JpaCall {

    @Id
    private long id;

    @Column
    private String phoneNumber;

    @Enumerated
    private Set<Skill> requiredSkills;

    @Column
    private Duration duration = Duration.ZERO;

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime pickUpTime;

    @Column
    private boolean pinned;

    @Column
    private Duration estimatedWaiting;

    @OneToOne
    private JpaCall nextCall;

    public JpaCall(long id, String phoneNumber, Set<Skill> requiredSkills, Duration duration, LocalTime startTime,
                   LocalTime pickUpTime, boolean pinned, Duration estimatedWaiting, JpaCall nextCall) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.requiredSkills = requiredSkills;
        this.duration = duration;
        this.startTime = startTime;
        this.pickUpTime = pickUpTime;
        this.pinned = pinned;
        this.estimatedWaiting = estimatedWaiting;
        this.nextCall = nextCall;
    }

    public long getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Set<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getPickUpTime() {
        return pickUpTime;
    }

    public boolean isPinned() {
        return pinned;
    }

    public Duration getEstimatedWaiting() {
        return estimatedWaiting;
    }

    public JpaCall getNextCall() {
        return nextCall;
    }
}
