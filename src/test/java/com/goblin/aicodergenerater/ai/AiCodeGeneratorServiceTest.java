package com.goblin.aicodergenerater.ai;

import com.goblin.aicodergenerater.ai.model.HtmlCodeResult;
import com.goblin.aicodergenerater.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author goblin
 * @Date 2025/8/5 16:54
 * @注释
 */
@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;


    @Test
    void generateCode() {
        String result = aiCodeGeneratorService.chat ("你好，你是谁？");
        System.out.println(result);
        Assertions.assertNotNull(result);
    }
    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个程序员的工作记录小工具");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("做个程序员的留言板");
        Assertions.assertNotNull(multiFileCode);
    }
}