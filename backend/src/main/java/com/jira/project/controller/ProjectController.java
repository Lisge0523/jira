package com.jira.project.controller;


import com.jira.common.PageResult;
import com.jira.common.Result;
import com.jira.project.dto.ProjectCreateRequest;
import com.jira.project.dto.ProjectUpdateRequest;
import com.jira.project.entity.Project;
import com.jira.project.service.ProjectService;
import com.jira.security.LoginInterceptor;
import jakarta.validation.Valid;
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
    @PostMapping("/projects")
    public Result<Project> create(
            @Valid @RequestBody ProjectCreateRequest request,
            @RequestAttribute(LoginInterceptor.ATTR_USER_ID) Long userId ) {
        return Result.ok(projectService.create(request,userId));
    }
    @PatchMapping("/projects/{id}")
    public Result<Project> update(
            @PathVariable Long id,
            @RequestBody ProjectUpdateRequest request,
            @RequestAttribute(LoginInterceptor.ATTR_USER_ID) Long userId){
        return Result.ok(projectService.update(id,request,userId));
    }
    @DeleteMapping("/projects/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestAttribute(LoginInterceptor.ATTR_USER_ID) Long userId
            ){
        projectService.delete(id,userId);
        return Result.ok();
    }
}
