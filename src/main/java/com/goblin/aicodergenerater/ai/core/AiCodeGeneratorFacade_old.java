//package com.goblin.aicodergenerater.ai.core;
//
//import com.goblin.aicodergenerater.ai.AiCodeGeneratorService;
//import com.goblin.aicodergenerater.ai.enums.CodeGenTypeEnum;
//import com.goblin.aicodergenerater.ai.model.HtmlCodeResult;
//import com.goblin.aicodergenerater.ai.model.MultiFileCodeResult;
//import com.goblin.aicodergenerater.exception.BusinessException;
//import com.goblin.aicodergenerater.exception.ErrorCode;
//import com.goblin.aicodergenerater.exception.ThrowUtils;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//
//import java.io.File;
//
///**
// * AI 代码生成外观类，组合生成和保存功能
// * 使用：门面模式
// */
//@Slf4j
////@Service
//public class AiCodeGeneratorFacade_old {
//
//    @Resource
//    private AiCodeGeneratorService aiCodeGeneratorService;
//
//    /**
//     * 统一入口：根据类型生成并保存代码
//     * @param userMessage
//     * @param codeGenTypeEnum
//     * @return
//     */
//    public File generateAndSaveCode(String userMessage , CodeGenTypeEnum codeGenTypeEnum){
//        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.SYSTEM_ERROR,"生成类型为空");
//        return switch (codeGenTypeEnum){
//            case HTML -> generateAndSaveHtmlCode(userMessage);
//            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
//            default -> {
//                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
//            }
//        };
//    }
//
//    /**
//     * 统一入口：根据类型生成并保存代码（流式）
//     *
//     * @param userMessage     用户提示词
//     * @param codeGenTypeEnum 生成类型
//     */
//    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
//        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.SYSTEM_ERROR,"生成类型为空");
//        return switch (codeGenTypeEnum){
//            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
//            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
//            default -> {
//                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR,errorMessage);
//            }
//        };
//    }
//
//
//    /**
//     * 生成 HTML 模式的代码并保存为文件
//     * @param userMessage
//     * @return
//     */
//    private File generateAndSaveHtmlCode(String userMessage){
//        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
//        return CodeFileSaver.saveHtmlCodeResult(result);
//    }
//
//    /**
//     * 生成多文件模式的代码并保存到文件
//     * @param userMessage
//     * @return
//     */
//    private File generateAndSaveMultiFileCode(String userMessage){
//        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
//        return CodeFileSaver.saveMultiFileCodeResult(result);
//    }
//
//    /**
//     * 生成 HTML 模式的代码并保存（流式）
//     *
//     * @param userMessage 用户提示词
//     * @return 保存的目录
//     */
//    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage){
//        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
//        // 当流式返回生成代码完成后，再取出并保存代码
//        StringBuilder codeBuilder = new StringBuilder();
//        return result.doOnNext(chunk -> {
//            // 实时收集ai的回复内容
//            codeBuilder.append(chunk);
//        })
//                .doOnComplete(() -> {
//                    // 流式返回结束后取出并保存代码
//                    try {
//                        String completeResult = codeBuilder.toString();
//                        HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeResult);
//                        // 保存代码到文件
//                        File savedDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
//                        log.info("HTML代码保存成功，保存目录：{}", savedDir.getAbsolutePath());
//                    }catch (Exception e){
//                        log.error("HTML代码保存失败：{}", e.getMessage());
//                    }
//                });
//    }
//
//    /**
//     * 生成多文件模式的代码并保存（流式）
//     *
//     * @param userMessage 用户提示词
//     * @return 保存的目录
//     */
//    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
//        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
//        // 当流式返回生成代码完成后，再取出并保存代码
//        StringBuilder codeBuilder = new StringBuilder();
//        // 实时收集ai的回复内容
//        return result.doOnNext(codeBuilder::append)
//                .doOnComplete(() -> {
//                    // 流式返回结束后取出并保存代码
//                    try {
//                        String completeResult = codeBuilder.toString();
//                        MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeResult);
//                        // 保存代码到文件
//                        File savedDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
//                        log.info("多文件代码保存成功，保存目录：{}", savedDir.getAbsolutePath());
//                    }catch (Exception e){
//                        log.error("多文件代码保存失败：{}", e.getMessage());
//                    }
//                });
//    }
//}
