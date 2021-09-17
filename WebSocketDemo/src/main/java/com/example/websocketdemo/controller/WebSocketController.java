package com.example.websocketdemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Excelman
 * @date 2021/9/16 上午10:20
 * @description
 */
@RestController
@RequestMapping("/api")
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate template;

    /**
     * 执行压缩，并返回进度条
     */
    @PostMapping("/test")
    public void process(@RequestParam("taskId") String taskId) throws InterruptedException {
        // 50%
        this.template.convertAndSendToUser(taskId, "/queue/getResponse", "50%");

        Thread.sleep(1000);

        // 100%
        this.template.convertAndSendToUser(taskId, "/queue/getResponse", "100%");
    }
}
