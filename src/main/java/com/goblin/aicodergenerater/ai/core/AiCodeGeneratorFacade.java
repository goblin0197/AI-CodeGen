package com.goblin.aicodergenerater.ai.core;

import cn.hutool.json.JSONUtil;
import com.goblin.aicodergenerater.ai.AiCodeGeneratorService;
import com.goblin.aicodergenerater.ai.AiCodeGeneratorServiceFactory;
import com.goblin.aicodergenerater.ai.enums.CodeGenTypeEnum;
import com.goblin.aicodergenerater.ai.model.HtmlCodeResult;
import com.goblin.aicodergenerater.ai.model.MultiFileCodeResult;
import com.goblin.aicodergenerater.ai.model.message.AiResponseMessage;
import com.goblin.aicodergenerater.ai.model.message.ToolExecutedMessage;
import com.goblin.aicodergenerater.ai.model.message.ToolRequestMessage;
import com.goblin.aicodergenerater.core.parse.CodeParserExecutor;
import com.goblin.aicodergenerater.core.saver.CodeFileSaverExecutor;
import com.goblin.aicodergenerater.exception.BusinessException;
import com.goblin.aicodergenerater.exception.ErrorCode;
import com.goblin.aicodergenerater.exception.ThrowUtils;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 * 使用：门面模式
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade{

//    @Resource
//    private AiCodeGeneratorService aiCodeGeneratorService;

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;
    /**
     * 统一入口：根据类型生成并保存代码
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public File generateAndSaveCode(String userMessage , CodeGenTypeEnum codeGenTypeEnum,Long appId){
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.SYSTEM_ERROR,"生成类型为空");
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,codeGenTypeEnum);
        return switch (codeGenTypeEnum){
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.SYSTEM_ERROR,"生成类型为空");
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,codeGenTypeEnum);
        return switch (codeGenTypeEnum){
            case HTML -> {
                Flux<String> htmlCodeResult = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(htmlCodeResult, CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                Flux<String> multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeTokenStream(appId, userMessage);
                Flux<String> codeStream = processTokenStream(tokenStream);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
            }
        };
    }


    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType,Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType,appId);
                log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }


//    private Flux<String> processTokenStream(TokenStream tokenStream) {
//        return Flux.create(sink -> {
//            tokenStream
//                .beforeToolExecution((BeforeToolExecution toolExecution) -> {
//                    System.out.println("1 准备调用工具beforeToolExecution ::AI 正在调用 "+toolExecution.request().name());
//                })
//                .onPartialThinking((PartialThinking partialThinking) -> {
//                    System.out.println(" 思考内容 onPartialThinking ::{" + partialThinking + "}");
//                })
//                .onPartialResponse((String partialResponse) -> {
//                    System.out.println("3 部分工具返回 onPartialResponse ::{" + partialResponse + "}");
//                })
////                        .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
////                            System.out.println("{" + toolExecutionRequest + "}");
////                        })
//                .onToolExecuted((ToolExecution toolExecution) -> {
//                    System.out.println("2 工具调用完成 onToolExecuted ::{" + toolExecution.result()+ "}");
//                })
//                .onCompleteResponse((ChatResponse response) -> {
//                    System.out.println("4 完成后返回 onCompleteResponse::{" + response + "}");
//                })
//                .onError((Throwable error) -> {
//                    error.printStackTrace();
//                })
//                .start();
//        });
//    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream
                    .onPartialResponse((String partialResponse) -> {
                        log.info("触发onPartialResponse: {}", partialResponse); // 打印部分响应
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        log.info("触发onPartialToolExecutionRequest: {}", toolExecutionRequest.toString());
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> { // 工具执行结果
                        log.info("触发onToolExecuted：{}",toolExecution.request().toString());
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
//                        log.info("触发onCompleteResponse: {}", response.toString());
                        sink.complete();
                    })
                    .onPartialThinking((PartialThinking partialThinking) -> {
                        log.info("触发onPartialThinking: {}", partialThinking.text());
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }


}
