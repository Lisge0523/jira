package com.jira.project.controller;


import com.jira.common.PageResult;
import com.jira.common.Result;
import com.jira.project.entity.Project;
import com.jira.project.service.ProjectService;
import com.jira.security.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/projects/{id}")
    public Result<Project> getById(@PathVariable Long id,
                                   @RequestAttribute(LoginInterceptor.ATTR_USER_ID) Long userId) {
        return Result.ok(projectService.getById(id,userId));
    }
    @GetMapping("/projects")
    public Result<PageResult<Project>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long personId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute(LoginInterceptor.ATTR_USER_ID) Long userId) {
        return Result.ok(projectService.list(name, personId, page, size,userId));
    }
}
