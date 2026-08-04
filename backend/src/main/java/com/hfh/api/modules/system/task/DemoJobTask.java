package com.hfh.api.modules.system.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DemoJobTask {

    public void sayHello(String jobName) {
        log.info("HELLO {}", jobName);
    }
}
