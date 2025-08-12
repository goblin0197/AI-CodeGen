package com.goblin.aicodergenerater.ai.core;

import com.goblin.aicodergenerater.ai.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author goblin
 * @Date 2025/8/5 21:43
 * @注释
 */
@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Test
    void testGenerateAndSaveCode() {
        File files = aiCodeGeneratorFacade.generateAndSaveCode("无需生成网站，只需要输出”你好你啊和“", CodeGenTypeEnum.MULTI_FILE,1L);
        Assertions.assertNotNull(files);
    }


    @Test
    void generateAndSaveCodeStream() {
//        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("任务记录网站", CodeGenTypeEnum.MULTI_FILE,1L);
//        // 阻塞等待所有数据收集完成
//        List<String> result = codeStream.collectList().block();
//        // 验证结果
//        Assertions.assertNotNull(result);
//        String completeContent = String.join("", result);
//        Assertions.assertNotNull(completeContent);

        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "将内容”你好不好？“保存到文件abc.txt中",
                CodeGenTypeEnum.VUE_PROJECT, 1L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

    @Test
    void generateVueProjectCodeTokenStream() {
    }

    @Test
    void testGenerateAndSaveCodeStream() {
    }
}