package com.goblin.aicodergenerater.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
public class MultiFileCodeResult {

    @Description("The code of html.")
    private String htmlCode;

    @Description("The code of css.")
    private String cssCode;

    @Description("The code of js.")
    private String jsCode;

    @Description("The summary of all code.")
    private String description;
}
