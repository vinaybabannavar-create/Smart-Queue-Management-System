package com.example.queue.service;

import com.example.queue.model.Token;
import com.example.queue.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class QueueService {

    @Autowired
    private TokenRepository tokenRepository;

    private Map<String, AtomicInteger> counters = new HashMap<>();

    public Token createToken(String branch, String category) {
        return createToken(branch, category, null, null, null, null);
    }

    public Token createToken(String branch, String category,
                             String patientName, Integer age, String phone, String symptoms) {

        String key = branch + "|" + category;
        counters.putIfAbsent(key, new AtomicInteger(0));
        int num = counters.get(key).incrementAndGet();
        String code = category.substring(0,1) + "-" + branch + "-" + String.format("%03d", num);

        Token t = new Token();
        t.setTokenCode(code);
        t.setBranch(branch);
        t.setPriorityCategory(category);
        t.setPatientName(patientName);
        t.setPatientAge(age);
        t.setPatientPhone(phone);
        t.setSymptoms(symptoms);
        if ("EMERGENCY".equalsIgnoreCase(category)) t.setEmergencyFlag(true);

        switch (category) {
            case "EMERGENCY": t.setEstimatedMinutes(2); break;
            case "VIP": t.setEstimatedMinutes(3); break;
            case "SENIOR": t.setEstimatedMinutes(4); break;
            default: t.setEstimatedMinutes(6);
        }
        tokenRepository.save(t);
        return t;
    }

    public Optional<Token> getNextForBranch(String branch) {
        List<String> priorityOrder = List.of("EMERGENCY","VIP","SENIOR","NORMAL");
        for (String p : priorityOrder) {
            List<Token> list = tokenRepository.findByBranchAndStatusOrderByCreatedAt(branch,"WAITING")
                    .stream().filter(token -> p.equals(token.getPriorityCategory()))
                    .collect(Collectors.toList());
            if (!list.isEmpty()) return Optional.of(list.get(0));
        }
        return Optional.empty();
    }

    public List<Token> getWaiting(String branch) {
        return tokenRepository.findByBranchAndStatusOrderByCreatedAt(branch,"WAITING");
    }

    public void markServing(Token token, String counter) {
        token.setStatus("SERVING");
        token.setCounter(counter);
        tokenRepository.save(token);
    }

    public void markCompleted(Token token) {
        token.setStatus("COMPLETED");
        tokenRepository.save(token);
    }

    public void skipToken(Token token) {
        token.setStatus("SKIPPED");
        token.setMissed(true);
        tokenRepository.save(token);
    }

    public double averageServiceMinutes(String branch) {
        List<Token> served = tokenRepository.findByBranchOrderByCreatedAt(branch)
                .stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .collect(Collectors.toList());
        if (served.isEmpty()) return 6.0;
        return served.stream().mapToDouble(Token::getEstimatedMinutes).average().orElse(6.0);
    }

    public String suggestCounter(Map<String,Integer> counterLoads) {
        return counterLoads.entrySet().stream().min(Comparator.comparingInt(Map.Entry::getValue))
                .map(e -> e.getKey()).orElse("Counter-1");
    }

    public void expireOldTokens(int thresholdMinutes) {
        List<Token> waiting = tokenRepository.findByStatusOrderByCreatedAt("WAITING");
        Instant now = Instant.now();
        for (Token t : waiting) {
            long minutes = Duration.between(t.getCreatedAt(), now).toMinutes();
            if (minutes >= thresholdMinutes) {
                t.setStatus("SKIPPED");
                t.setMissed(true);
                tokenRepository.save(t);
            }
        }
    }

    public Optional<Token> findByCode(String code){
        return tokenRepository.findAll().stream().filter(t->t.getTokenCode().equals(code)).findFirst();
    }

    // FIXED: return latest (not oldest) serving token
    public Optional<Token> getCurrentlyServing(String branch){
        List<Token> list = tokenRepository.findByBranchAndStatusOrderByCreatedAt(branch,"SERVING");
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(list.size() - 1));
    }
}
