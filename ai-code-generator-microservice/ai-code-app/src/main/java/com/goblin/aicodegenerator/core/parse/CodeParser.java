package com.goblin.aicodegenerator.core.parse;

/**
 * @Author goblin
 * @Date 2025/8/6 11:34
 * @注释
 */
public interface CodeParser <T>{
    /**
     * 解析代码内容
     * @param codeContent 原始内容
     * @return 解析后的结果对象
     */
    T parseCode(String codeContent);
}
