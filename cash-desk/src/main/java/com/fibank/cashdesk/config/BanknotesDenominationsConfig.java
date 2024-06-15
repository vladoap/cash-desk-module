package com.fibank.cashdesk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;


@ConfigurationProperties
@ConstructorBinding
public class BanknotesDenominationsConfig {

    private final Map<String, Set<Integer>> denominations;

    public BanknotesDenominationsConfig(Map<String, Set<Integer>> denominations) {
        this.denominations = Collections.unmodifiableMap(denominations);
    }


    public Map<String, Set<Integer>> getDenominations() {
        return denominations;
    }
}
