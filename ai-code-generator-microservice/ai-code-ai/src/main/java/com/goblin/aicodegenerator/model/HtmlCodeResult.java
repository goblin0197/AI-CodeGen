package com.goblin.aicodegenerator.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
public class HtmlCodeResult {

    @Description("The code of html.")
    private String htmlCode;

    @Description("The description of html.")
    private String description;
}
