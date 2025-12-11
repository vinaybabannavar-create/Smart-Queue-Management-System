package com.example.queue.repository;

import com.example.queue.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findByBranchAndStatusOrderByCreatedAt(String branch, String status);
    List<Token> findByStatusOrderByCreatedAt(String status);
    List<Token> findByBranchOrderByCreatedAt(String branch);
}
