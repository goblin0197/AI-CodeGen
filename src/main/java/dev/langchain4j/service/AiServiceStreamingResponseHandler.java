//BiConsumer<Integer, ToolExecutionRequest> partialToolExecutionRequestHandler, this.partialToolExecutionRequestHandler = partialToolExecutionRequestHandler;
//@Override
//public void onPartialToolExecutionRequest(int index, ToolExecutionRequest partialToolExecutionRequest) {
//    // If we're using output guardrails, then buffer the partial response until the guardrails have completed
//    partialToolExecutionRequestHandler.accept(index, partialToolExecutionRequest);
//}

package dev.langchain4j.service;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.guardrail.ChatExecutor;
import dev.langchain4j.guardrail.GuardrailRequestParams;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
class AiServiceStreamingResponseHandler implements StreamingChatResponseHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AiServiceStreamingResponseHandler.class);
    private final ChatExecutor chatExecutor;
    private final AiServiceContext context;
    private final Object memoryId;
    private final GuardrailRequestParams commonGuardrailParams;
    private final Object methodKey;
    private final Consumer<String> partialResponseHandler;
    private final Consumer<PartialThinking> partialThinkingHandler;
    private final Consumer<BeforeToolExecution> beforeToolExecutionHandler;
    private final Consumer<ToolExecution> toolExecutionHandler;
    private final Consumer<ChatResponse> intermediateResponseHandler;
    private final Consumer<ChatResponse> completeResponseHandler;
    private final Consumer<Throwable> errorHandler;
    private final ChatMemory temporaryMemory;
    private final TokenUsage tokenUsage;
    private final List<ToolSpecification> toolSpecifications;
    private final Map<String, ToolExecutor> toolExecutors;
    private final List<String> responseBuffer = new ArrayList();
    private final boolean hasOutputGuardrails;
    private final BiConsumer<Integer, ToolExecutionRequest> partialToolExecutionRequestHandler;
    private final BiConsumer<Integer, ToolExecutionRequest> completeToolExecutionRequestHandler;

    AiServiceStreamingResponseHandler(BiConsumer<Integer, ToolExecutionRequest> completeToolExecutionRequestHandler,BiConsumer<Integer, ToolExecutionRequest> partialToolExecutionRequestHandler, ChatExecutor chatExecutor, AiServiceContext context, Object memoryId, Consumer<String> partialResponseHandler, Consumer<PartialThinking> partialThinkingHandler, Consumer<BeforeToolExecution> beforeToolExecutionHandler, Consumer<ToolExecution> toolExecutionHandler, Consumer<ChatResponse> intermediateResponseHandler, Consumer<ChatResponse> completeResponseHandler, Consumer<Throwable> errorHandler, ChatMemory temporaryMemory, TokenUsage tokenUsage, List<ToolSpecification> toolSpecifications, Map<String, ToolExecutor> toolExecutors, GuardrailRequestParams commonGuardrailParams, Object methodKey) {
        this.completeToolExecutionRequestHandler = completeToolExecutionRequestHandler;
        this.partialToolExecutionRequestHandler = partialToolExecutionRequestHandler;
        this.chatExecutor = (ChatExecutor)ValidationUtils.ensureNotNull(chatExecutor, "chatExecutor");
        this.context = (AiServiceContext)ValidationUtils.ensureNotNull(context, "context");
        this.memoryId = ValidationUtils.ensureNotNull(memoryId, "memoryId");
        this.methodKey = methodKey;
        this.partialResponseHandler = (Consumer)ValidationUtils.ensureNotNull(partialResponseHandler, "partialResponseHandler");
        this.partialThinkingHandler = partialThinkingHandler;
        this.intermediateResponseHandler = intermediateResponseHandler;
        this.completeResponseHandler = completeResponseHandler;
        this.beforeToolExecutionHandler = beforeToolExecutionHandler;
        this.toolExecutionHandler = toolExecutionHandler;
        this.errorHandler = errorHandler;
        this.temporaryMemory = temporaryMemory;
        this.tokenUsage = (TokenUsage)ValidationUtils.ensureNotNull(tokenUsage, "tokenUsage");
        this.commonGuardrailParams = commonGuardrailParams;
        this.toolSpecifications = Utils.copy(toolSpecifications);
        this.toolExecutors = Utils.copy(toolExecutors);
        this.hasOutputGuardrails = context.guardrailService().hasOutputGuardrails(methodKey);
    }
    @Override
    public void onPartialToolExecutionRequest(int index, ToolExecutionRequest partialToolExecutionRequest) {
        // If we're using output guardrails, then buffer the partial response until the guardrails have completed
        partialToolExecutionRequestHandler.accept(index, partialToolExecutionRequest);
    }

    public void onPartialResponse(String partialResponse) {
        if (this.hasOutputGuardrails) {
            this.responseBuffer.add(partialResponse);
        } else {
            this.partialResponseHandler.accept(partialResponse);
        }

    }

    public void onPartialThinking(PartialThinking partialThinking) {
        if (this.partialThinkingHandler != null) {
            this.partialThinkingHandler.accept(partialThinking);
        }

    }

    public void onCompleteResponse(ChatResponse chatResponse) {
        AiMessage aiMessage = chatResponse.aiMessage();
        this.addToMemory(aiMessage);
        if (aiMessage.hasToolExecutionRequests()) {
            if (this.intermediateResponseHandler != null) {
                this.intermediateResponseHandler.accept(chatResponse);
            }

            for(ToolExecutionRequest toolExecutionRequest : aiMessage.toolExecutionRequests()) {
                if (this.beforeToolExecutionHandler != null) {
                    BeforeToolExecution beforeToolExecution = BeforeToolExecution.builder().request(toolExecutionRequest).build();
                    this.beforeToolExecutionHandler.accept(beforeToolExecution);
                }

                String toolName = toolExecutionRequest.name();
                ToolExecutor toolExecutor = (ToolExecutor)this.toolExecutors.get(toolName);
                String toolExecutionResult = toolExecutor.execute(toolExecutionRequest, this.memoryId);
                ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(toolExecutionRequest, toolExecutionResult);
                this.addToMemory(toolExecutionResultMessage);
                if (this.toolExecutionHandler != null) {
                    ToolExecution toolExecution = ToolExecution.builder().request(toolExecutionRequest).result(toolExecutionResult).build();
                    this.toolExecutionHandler.accept(toolExecution);
                }
            }

            ChatRequest chatRequest = ChatRequest.builder().messages(this.messagesToSend(this.memoryId)).toolSpecifications(this.toolSpecifications).build();
            AiServiceStreamingResponseHandler handler = new AiServiceStreamingResponseHandler(this.completeToolExecutionRequestHandler,this.partialToolExecutionRequestHandler,this.chatExecutor, this.context, this.memoryId, this.partialResponseHandler, this.partialThinkingHandler, this.beforeToolExecutionHandler, this.toolExecutionHandler, this.intermediateResponseHandler, this.completeResponseHandler, this.errorHandler, this.temporaryMemory, TokenUsage.sum(this.tokenUsage, chatResponse.metadata().tokenUsage()), this.toolSpecifications, this.toolExecutors, this.commonGuardrailParams, this.methodKey);
            this.context.streamingChatModel.chat(chatRequest, handler);
        } else if (this.completeResponseHandler != null) {
            ChatResponse finalChatResponse = ChatResponse.builder().aiMessage(aiMessage).metadata(chatResponse.metadata().toBuilder().tokenUsage(this.tokenUsage.add(chatResponse.metadata().tokenUsage())).build()).build();
            if (this.hasOutputGuardrails) {
                if (this.commonGuardrailParams != null) {
                    GuardrailRequestParams newCommonParams = GuardrailRequestParams.builder().chatMemory(this.getMemory()).augmentationResult(this.commonGuardrailParams.augmentationResult()).userMessageTemplate(this.commonGuardrailParams.userMessageTemplate()).variables(this.commonGuardrailParams.variables()).build();
                    OutputGuardrailRequest outputGuardrailParams = OutputGuardrailRequest.builder().responseFromLLM(finalChatResponse).chatExecutor(this.chatExecutor).requestParams(newCommonParams).build();
                    finalChatResponse = (ChatResponse)this.context.guardrailService().executeGuardrails(this.methodKey, outputGuardrailParams);
                }

                List var10000 = this.responseBuffer;
                Consumer var10001 = this.partialResponseHandler;
                Objects.requireNonNull(var10001);
                var10000.forEach(var10001::accept);
                this.responseBuffer.clear();
            }

            this.completeResponseHandler.accept(finalChatResponse);
        }

    }

    private ChatMemory getMemory() {
        return this.getMemory(this.memoryId);
    }

    private ChatMemory getMemory(Object memId) {
        return this.context.hasChatMemory() ? this.context.chatMemoryService.getOrCreateChatMemory(this.memoryId) : this.temporaryMemory;
    }

    private void addToMemory(ChatMessage chatMessage) {
        this.getMemory().add(chatMessage);
    }

    private List<ChatMessage> messagesToSend(Object memoryId) {
        return this.getMemory(memoryId).messages();
    }

    public void onError(Throwable error) {
        if (this.errorHandler != null) {
            try {
                this.errorHandler.accept(error);
            } catch (Exception e) {
                LOG.error("While handling the following error...", error);
                LOG.error("...the following error happened", e);
            }
        } else {
            LOG.warn("Ignored error", error);
        }

    }
}
