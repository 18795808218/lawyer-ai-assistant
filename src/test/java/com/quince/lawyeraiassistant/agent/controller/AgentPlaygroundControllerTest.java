package com.quince.lawyeraiassistant.agent.controller;

import com.quince.lawyeraiassistant.agent.dto.AgentRequest;
import com.quince.lawyeraiassistant.agent.model.AgentContext;
import com.quince.lawyeraiassistant.agent.model.AgentStatus;
import com.quince.lawyeraiassistant.agent.pipeline.AgentPipeline;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgentPlaygroundController.class)
class AgentPlaygroundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AgentPipeline agentPipeline;

    @Test
    void shouldExecuteAgentPipelineAndReturnResponse()
            throws Exception {

        AgentContext resultContext = AgentContext.builder()
                .goal(
                        "分析劳动合同并生成律师意见书")
                .status(
                        AgentStatus.RUNNING)
                .executionLogs(
                        List.of(
                                "Reason completed",
                                "Planning completed"))
                .build();

        when(
                agentPipeline.execute(
                        any(AgentContext.class)))
                .thenReturn(resultContext);

        AgentRequest request = new AgentRequest(
                "分析劳动合同并生成律师意见书");

        mockMvc.perform(
                post("/api/playground/agent")
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        request)))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.goal")
                                .value(
                                        "分析劳动合同并生成律师意见书"))
                .andExpect(
                        jsonPath("$.status")
                                .value("RUNNING"))
                .andExpect(
                        jsonPath("$.executionLogs[0]")
                                .value(
                                        "Reason completed"))
                .andExpect(
                        jsonPath("$.executionLogs[1]")
                                .value(
                                        "Planning completed"));

        verify(
                agentPipeline).execute(
                        any(AgentContext.class));
    }

    @Test
    void shouldRejectBlankGoal()
            throws Exception {

        AgentRequest request = new AgentRequest("   ");

        mockMvc.perform(
                post("/api/playground/agent")
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        request)))
                .andExpect(
                        status().isBadRequest());

        verify(
                agentPipeline,
                never()).execute(
                        any(AgentContext.class));
    }

    @Test
    void shouldRejectMissingGoal()
            throws Exception {

        mockMvc.perform(
                post("/api/playground/agent")
                        .contentType(
                                MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(
                        status().isBadRequest());

        verify(
                agentPipeline,
                never()).execute(
                        any(AgentContext.class));
    }
}