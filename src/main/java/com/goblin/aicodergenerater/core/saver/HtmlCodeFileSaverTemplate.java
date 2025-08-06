package com.goblin.aicodergenerater.core.saver;

import cn.hutool.core.util.StrUtil;
import com.goblin.aicodergenerater.ai.enums.CodeGenTypeEnum;
import com.goblin.aicodergenerater.ai.model.HtmlCodeResult;
import com.goblin.aicodergenerater.exception.BusinessException;
import com.goblin.aicodergenerater.exception.ErrorCode;

/**
 * HTML代码文件保存器
 *
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        // 保存 HTML 文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        // HTML 代码不为空
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}