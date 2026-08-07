package com.quince.lawyeraiassistant.agent.controller;

import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentPlan;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;
import com.quince.lawyeraiassistant.agent.model.AgentTask;
import com.quince.lawyeraiassistant.agent.model.AgentTaskStatus;
import com.quince.lawyeraiassistant.agent.model.ReasonResult;
import com.quince.lawyeraiassistant.agent.model.ToolObservation;
import com.quince.lawyeraiassistant.agent.runtime.AgentRuntime;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AgentPlaygroundControllerTest {

        private AgentRuntime agentRuntime;

        private MockMvc mockMvc;

        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {

                agentRuntime = mock(
                                AgentRuntime.class);

                AgentPlaygroundController controller = new AgentPlaygroundController(
                                agentRuntime);

                mockMvc = MockMvcBuilders
                                .standaloneSetup(
                                                controller)
                                .build();

                objectMapper = new ObjectMapper();
        }

        @Test
        void shouldRunAgentRuntimeAndReturnResponse()
                        throws Exception {

                AgentContext resultContext = AgentContext.builder()
                                .goal(
                                                "查询违法解除劳动合同需要承担什么法律责任")
                                .reasonResult(
                                                ReasonResult.from(
                                                                "用户希望了解违法解除劳动合同所对应的法律责任。"))
                                .agentPlan(
                                                AgentPlan.from(
                                                                List.of(
                                                                                AgentTask.pending(
                                                                                                "task-1",
                                                                                                "查询劳动合同法相关规定")
                                                                                                .withStatus(
                                                                                                                AgentTaskStatus.COMPLETED),
                                                                                AgentTask.pending(
                                                                                                "task-2",
                                                                                                "查询违法解除的法律责任")
                                                                                                .withStatus(
                                                                                                                AgentTaskStatus.COMPLETED))))
                                .observations(
                                                List.of(
                                                                ToolObservation.success(
                                                                                "task-1",
                                                                                "searchLegalKnowledge",
                                                                                "检索到劳动合同法相关规定。"),
                                                                ToolObservation.success(
                                                                                "task-2",
                                                                                "searchLegalKnowledge",
                                                                                "检索到违法解除劳动合同的法律责任。")))
                                .status(
                                                AgentStatus.FINISHED)
                                .executionLogs(
                                                List.of(
                                                                "Reason completed",
                                                                "Planning completed",
                                                                "Tool execution completed: task-1",
                                                                "Tool execution completed: task-2",
                                                                "Agent finished"))
                                .build();

                when(
                                agentRuntime.run(
                                                any(
                                                                AgentContext.class)))
                                .thenReturn(
                                                resultContext);

                String requestBody = """
                                {
                                    "goal": "查询违法解除劳动合同需要承担什么法律责任"
                                }
                                """;

                mockMvc.perform(
                                post(
                                                "/api/playground/agent")
                                                .contentType(
                                                                MediaType.APPLICATION_JSON)
                                                .content(
                                                                requestBody))
                                .andExpect(
                                                status().isOk())
                                .andExpect(
                                                jsonPath("$.goal")
                                                                .value(
                                                                                "查询违法解除劳动合同需要承担什么法律责任"))
                                .andExpect(
                                                jsonPath("$.reasonSummary")
                                                                .value(
                                                                                "用户希望了解违法解除劳动合同所对应的法律责任。"))
                                .andExpect(
                                                jsonPath("$.plan.length()")
                                                                .value(2))
                                .andExpect(
                                                jsonPath("$.plan[0].id")
                                                                .value(
                                                                                "task-1"))
                                .andExpect(
                                                jsonPath("$.plan[0].status")
                                                                .value(
                                                                                "COMPLETED"))
                                .andExpect(
                                                jsonPath("$.plan[1].id")
                                                                .value(
                                                                                "task-2"))
                                .andExpect(
                                                jsonPath("$.plan[1].status")
                                                                .value(
                                                                                "COMPLETED"))
                                .andExpect(
                                                jsonPath("$.observations.length()")
                                                                .value(2))
                                .andExpect(
                                                jsonPath("$.observations[0].taskId")
                                                                .value(
                                                                                "task-1"))
                                .andExpect(
                                                jsonPath("$.observations[0].toolName")
                                                                .value(
                                                                                "searchLegalKnowledge"))
                                .andExpect(
                                                jsonPath("$.observations[0].success")
                                                                .value(true))
                                .andExpect(
                                                jsonPath("$.observations[1].taskId")
                                                                .value(
                                                                                "task-2"))
                                .andExpect(
                                                jsonPath("$.observations[1].success")
                                                                .value(true))
                                .andExpect(
                                                jsonPath("$.status")
                                                                .value(
                                                                                "FINISHED"))
                                .andExpect(
                                                jsonPath("$.executionLogs.length()")
                                                                .value(5))
                                .andExpect(
                                                jsonPath("$.executionLogs[4]")
                                                                .value(
                                                                                "Agent finished"));

                verify(
                                agentRuntime)
                                .run(
                                                any(
                                                                AgentContext.class));
        }
}