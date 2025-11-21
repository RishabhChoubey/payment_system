package com.paypal.reward_service.controller;

import com.paypal.reward_service.entity.Reward;
import com.paypal.reward_service.repository.RewardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards") // normalized (removed trailing slash)
public class RewardController {
    private static final Logger log = LoggerFactory.getLogger(RewardController.class);
    private final RewardRepository rewardRepository;

    public RewardController(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    // Get all rewards
    @GetMapping({"", "/"}) // support both /api/rewards and /api/rewards/
    public ResponseEntity<List<Reward>> getAllRewards() {
        log.debug("GET /api/rewards - fetching all rewards");
        List<Reward> rewards = rewardRepository.findAll();
        return ResponseEntity.ok(rewards);
    }

    // Get rewards by user ID (support with or without double slash legacy)
    @GetMapping({"/user/{userId}", "//user/{userId}"})
    public ResponseEntity<List<Reward>> getRewardsByUserId(@PathVariable Long userId) {
        log.debug("GET /api/rewards/user/{} - fetching rewards for user", userId);
        List<Reward> rewards = rewardRepository.findByUserId(userId);
        return ResponseEntity.ok(rewards);
    }

    // Health/debug endpoint to confirm controller is registered
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.debug("GET /api/rewards/health - OK");
        return ResponseEntity.ok("REWARD-CONTROLLER-OK");
    }
}
