package com.jira.project.controller;


import com.jira.common.Result;
import com.jira.project.entity.Project;
import com.jira.project.service.ProjectService;
import com.jira.security.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/projects/{id}")
    public Result<Project> getById(@PathVariable Long id,
                                   @RequestAttribute(LoginInterceptor.ATTR_USER_ID) Long userId) {
        return Result.ok(projectService.getById(id,userId));
    }
}
