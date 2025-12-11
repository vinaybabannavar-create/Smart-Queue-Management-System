package com.example.queue.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tokenCode;
    private String branch;
    private String priorityCategory;
    private String counter;

    private Instant createdAt = Instant.now();

    private String status = "WAITING";

    private int estimatedMinutes = 5;

    private boolean missed = false;

    // Patient details
    private String patientName;
    private Integer patientAge;
    private String patientPhone;

    @Column(length = 1000)
    private String symptoms;

    private boolean emergencyFlag = false;

    // ---------- Getters / Setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenCode() { return tokenCode; }
    public void setTokenCode(String tokenCode) { this.tokenCode = tokenCode; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getPriorityCategory() { return priorityCategory; }
    public void setPriorityCategory(String priorityCategory) { this.priorityCategory = priorityCategory; }

    public String getCounter() { return counter; }
    public void setCounter(String counter) { this.counter = counter; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public boolean isMissed() { return missed; }
    public void setMissed(boolean missed) { this.missed = missed; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public boolean isEmergencyFlag() { return emergencyFlag; }
    public void setEmergencyFlag(boolean emergencyFlag) { this.emergencyFlag = emergencyFlag; }
}
