package com.quince.lawyeraiassistant.exception;

public class SensitiveWordException extends RuntimeException {

    private final String matchedWord;

    public SensitiveWordException(String matchedWord) {
        super("当前问题包含不允许处理的内容");
        this.matchedWord = matchedWord;
    }

    public String getMatchedWord() {
        return matchedWord;
    }
}