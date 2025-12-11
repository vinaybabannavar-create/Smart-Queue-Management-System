package com.example.queue.service;

import com.example.queue.model.Feedback;
import com.example.queue.model.Token;
import com.example.queue.repository.FeedbackRepository;
import com.example.queue.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class AnalyticsService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private TokenRepository tokenRepository;

    public double averageRating(){
        List<Feedback> all = feedbackRepository.findAll();
        if(all.isEmpty()) return 0.0;

        IntSummaryStatistics s = all.stream()
                .mapToInt(Feedback::getRating)
                .summaryStatistics();

        return s.getAverage();
    }

    public long totalVisitorsToday(){
        // simple: count tokens created today
        return tokenRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate().equals(LocalDate.now(ZoneOffset.UTC)))
                .count();
    }

    // return all tokens created today (for dashboard stats)
    public List<Token> allToday(){
        return tokenRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate().equals(LocalDate.now(ZoneOffset.UTC)))
                .collect(Collectors.toList());
    }
}
