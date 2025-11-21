package com.paypal.transaction_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.transaction_service.entity.Transaction;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionRepository repository;
    private final KafkaEventProducer kafkaEventProducer;

    public TransactionServiceImpl(TransactionRepository repository,
                                KafkaEventProducer kafkaEventProducer) {
        this.repository = repository;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    @Override
    public Transaction createTransaction(Transaction request) {
        logger.info("[START] Entered createTransaction()");

        Long senderId = request.getSenderId();
        Long receiverId = request.getReceiverId();
        Double amount = request.getAmount();

        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setReceiverId(receiverId);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");

        logger.info("[INPUT] Incoming Transaction object: {}", transaction);

        Transaction saved = repository.save(transaction);
        logger.info("[SAVED] Saved Transaction from DB: {}", saved);

        try {
            String key = String.valueOf(saved.getId());
            kafkaEventProducer.sendTransactionEvent(key, saved);
            logger.info("[KAFKA] Message sent successfully");
        } catch (Exception e) {
            logger.error("[ERROR] Failed to send Kafka event: {}", e.getMessage(), e);
        }

        return saved;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }
}
