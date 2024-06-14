package com.fibank.cashdesk.config;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration
@ConfigurationProperties
public class BanknotesDenominationsConfig {

    private Map<String, Set<Integer>> denominations;

    public Map<String, Set<Integer>> getDenominations() {
        return denominations;
    }

    public BanknotesDenominationsConfig setDenominations(Map<String, Set<Integer>> denominations) {
        this.denominations = denominations;
        return this;
    }
}
