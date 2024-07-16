package com.example.mini.global.security.token;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TokenProcessorFactory {
    private final Map<String, TokenProcessor> processors;

    public TokenProcessorFactory(List<TokenProcessor> processorList) {
        processors = processorList.stream()
                .collect(Collectors.toMap(p -> p.getClass().getSimpleName(), Function.identity()));
    }

    public TokenProcessor getProcessor(String tokenType) {
        return processors.get(tokenType + "TokenProcessor");
    }
}
