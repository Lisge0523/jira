package com.jira.system;

import com.jira.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 骨架自检接口。
 *
 * 启动后访问 http://localhost:8080/health
 * 期望：{"code":0,"message":"成功","data":{"status":"UP","userCount":4}}
 *
 * userCount 为 4 说明「Web 层 + MyBatis + MySQL」三段链路全部打通。
 *
 * 路径约定：本服务不加 context-path，也不加 /api 前缀，
 * 所有接口路径与 Mock 保持一致（/login、/me、/projects、/tasks ...），
 * 这样前端只需改 host:port。
 */
@RestController
public class HealthController {

    private final HealthMapper healthMapper;

    public HealthController(HealthMapper healthMapper) {
        this.healthMapper = healthMapper;
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("userCount", healthMapper.countUsers());
        return Result.ok(data);
    }
}
