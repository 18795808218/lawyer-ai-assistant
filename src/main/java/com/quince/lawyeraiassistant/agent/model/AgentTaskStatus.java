package com.quince.lawyeraiassistant.agent.model;

/**
 * Agent 任务执行状态。
 *
 * <p>
 * 用于描述 AgentPlan 中单个 AgentTask 的生命周期。
 * </p>
 */
public enum AgentTaskStatus {

    /**
     * 任务已创建，但尚未开始执行。
     */
    PENDING,

    /**
     * 任务正在执行。
     */
    RUNNING,

    /**
     * 任务已经成功完成。
     */
    COMPLETED,

    /**
     * 任务执行失败。
     */
    FAILED
}