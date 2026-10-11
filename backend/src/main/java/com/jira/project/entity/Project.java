package com.jira.project.entity;


import lombok.Data;

@Data
public class Project {
    private Long id;
    private String name;
    private Long personId;
    private String organization;
    private Long created;
    private Boolean pin;
}
