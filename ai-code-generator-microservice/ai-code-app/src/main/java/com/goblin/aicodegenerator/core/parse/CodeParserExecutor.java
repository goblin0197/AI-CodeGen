package com.goblin.aicodegenerator.core.parse;


import com.goblin.aicodegenerator.exception.BusinessException;
import com.goblin.aicodegenerator.exception.ErrorCode;
import com.goblin.aicodegenerator.model.enums.CodeGenTypeEnum;

/**
 * @Author goblin
 * @Date 2025/8/6 12:44
 * @注释
 */
public class CodeParserExecutor {
    public static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    public static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析
     *
     * @param codeContent 代码内容
     * @param codeGenType 代码生成类型
     * @return 解析结果（HtmlCodeResult 或 MultiFileCodeResult）
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}
