package com.quince.lawyeraiassistant.agent.service;

import com.quince.lawyeraiassistant.agent.model.AgentPlan;
import com.quince.lawyeraiassistant.agent.model.AgentTaskStatus;
import com.quince.lawyeraiassistant.agent.model.ReasonResult;
import com.quince.lawyeraiassistant.agent.prompt.model.PlanningPromptContext;
import com.quince.lawyeraiassistant.prompt.builder.PromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.prompt.Prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultAgentPlanningServiceTest {

    private ChatClient chatClient;

    private PromptBuilder promptBuilder;

    private ChatClientRequestSpec requestSpec;

    private CallResponseSpec responseSpec;

    private DefaultAgentPlanningService planningService;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class);

        promptBuilder = mock(PromptBuilder.class);

        requestSpec = mock(ChatClientRequestSpec.class);

        responseSpec = mock(CallResponseSpec.class);

        planningService = new DefaultAgentPlanningService(
                chatClient,
                promptBuilder);
    }

    @Test
    void shouldGenerateAgentPlan() {
        PlanningPromptContext context = PlanningPromptContext.from(
                "分析劳动合同并生成律师意见书",
                ReasonResult.from(
                        "用户希望分析劳动合同并生成律师意见书。"));

        Prompt prompt = mock(Prompt.class);

        when(
                promptBuilder.buildPlanning(
                        context))
                .thenReturn(prompt);

        when(
                chatClient.prompt(prompt)).thenReturn(requestSpec);

        when(
                requestSpec.call()).thenReturn(responseSpec);

        when(
                responseSpec.content()).thenReturn(
                        """
                                task-1|读取劳动合同
                                task-2|识别法律风险
                                task-3|生成律师意见书
                                """);

        AgentPlan result = planningService.plan(
                context);

        assertEquals(
                3,
                result.taskCount());

        assertEquals(
                "task-1",
                result.getTasks()
                        .get(0)
                        .getId());

        assertEquals(
                "读取劳动合同",
                result.getTasks()
                        .get(0)
                        .getDescription());

        assertEquals(
                AgentTaskStatus.PENDING,
                result.getTasks()
                        .get(0)
                        .getStatus());
    }

    @Test
    void shouldIgnoreBlankLines() {
        PlanningPromptContext context = createContext();

        Prompt prompt = mock(Prompt.class);

        when(
                promptBuilder.buildPlanning(
                        context))
                .thenReturn(prompt);

        when(
                chatClient.prompt(prompt)).thenReturn(requestSpec);

        when(
                requestSpec.call()).thenReturn(responseSpec);

        when(
                responseSpec.content()).thenReturn(
                        """

                                task-1|读取劳动合同

                                task-2|识别法律风险

                                """);

        AgentPlan result = planningService.plan(
                context);

        assertEquals(
                2,
                result.taskCount());
    }

    @Test
    void shouldRejectNullContext() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> planningService.plan(null));

        assertEquals(
                "PlanningPromptContext must not be null",
                exception.getMessage());
    }

    @Test
    void shouldRejectBlankPlanningResult() {
        PlanningPromptContext context = createContext();

        Prompt prompt = mock(Prompt.class);

        when(
                promptBuilder.buildPlanning(
                        context))
                .thenReturn(prompt);

        when(
                chatClient.prompt(prompt)).thenReturn(requestSpec);

        when(
                requestSpec.call()).thenReturn(responseSpec);

        when(
                responseSpec.content()).thenReturn("   ");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> planningService.plan(
                        context));

        assertEquals(
                "Planning result must not be blank",
                exception.getMessage());
    }

    @Test
    void shouldRejectInvalidTaskFormat() {
        PlanningPromptContext context = createContext();

        Prompt prompt = mock(Prompt.class);

        when(
                promptBuilder.buildPlanning(
                        context))
                .thenReturn(prompt);

        when(
                chatClient.prompt(prompt)).thenReturn(requestSpec);

        when(
                requestSpec.call()).thenReturn(responseSpec);

        when(
                responseSpec.content()).thenReturn(
                        "读取劳动合同");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> planningService.plan(
                        context));

        assertEquals(
                "Invalid planning task format: 读取劳动合同",
                exception.getMessage());
    }

    @Test
    void shouldRejectEmptyTaskDescription() {
        PlanningPromptContext context = createContext();

        Prompt prompt = mock(Prompt.class);

        when(
                promptBuilder.buildPlanning(
                        context))
                .thenReturn(prompt);

        when(
                chatClient.prompt(prompt)).thenReturn(requestSpec);

        when(
                requestSpec.call()).thenReturn(responseSpec);

        when(
                responseSpec.content()).thenReturn(
                        "task-1|   ");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> planningService.plan(
                        context));

        assertEquals(
                "Invalid planning task: task-1|",
                exception.getMessage());
    }

    private PlanningPromptContext createContext() {
        return PlanningPromptContext.from(
                "分析劳动合同",
                ReasonResult.from(
                        "用户希望分析劳动合同。"));
    }
}