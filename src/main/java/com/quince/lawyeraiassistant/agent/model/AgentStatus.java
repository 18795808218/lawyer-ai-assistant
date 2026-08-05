package com.quince.lawyeraiassistant.agent.model;

/**
 * Agent 当前执行状态。
 */
public enum AgentStatus {

    /**
     * AgentContext 已创建，但尚未开始执行。
     */
    CREATED,

    /**
     * Agent 正在执行。
     */
    RUNNING,

    /**
     * Agent 已成功完成目标。
     */
    FINISHED,

    /**
     * Agent 执行失败。
     */
    FAILED
}