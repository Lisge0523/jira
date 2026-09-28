package com.jira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Jira 后端启动类。
 *
 * 包结构约定（双人分工，见《后端重构调研文档》第八章）：
 *   common/    统一响应、异常          【成员 A】
 *   config/    跨域、MyBatis 等配置     【成员 A】
 *   security/  登录拦截、JWT、权限校验  【成员 A】
 *   user/      认证与用户              【成员 A】
 *   project/   项目 + 项目成员          【成员 A】
 *   tasktype/  任务类型                【成员 B】
 *   kanban/    看板列                  【成员 B】
 *   task/      任务                    【成员 B】
 *   epic/      任务组                  【成员 B】
 *
 * 每个模块内部统一分：controller / service / mapper / entity / dto
 */
@SpringBootApplication
public class JiraApplication {

    public static void main(String[] args) {
        SpringApplication.run(JiraApplication.class, args);
    }
}
