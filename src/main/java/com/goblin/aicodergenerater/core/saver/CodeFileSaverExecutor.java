package com.goblin.aicodergenerater.core.saver;

import com.goblin.aicodergenerater.ai.enums.CodeGenTypeEnum;
import com.goblin.aicodergenerater.ai.model.HtmlCodeResult;
import com.goblin.aicodergenerater.ai.model.MultiFileCodeResult;
import com.goblin.aicodergenerater.exception.BusinessException;
import com.goblin.aicodergenerater.exception.ErrorCode;

import java.io.File;

/**
 * 代码文件保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 *
 */
public class CodeFileSaverExecutor {
    public static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate = new HtmlCodeFileSaverTemplate();
    public static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaverTemplate = new MultiFileCodeFileSaverTemplate();

    /**
     * 执行代码保存
     *
     * @param codeResult  代码结果对象
     * @param codeGenType 代码生成类型
     * @return 保存的目录
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType){
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) codeResult);
            case MULTI_FILE -> multiFileCodeFileSaverTemplate.saveCode((MultiFileCodeResult) codeResult);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}