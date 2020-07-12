package com.iavorskyi.repos;

import com.iavorskyi.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Long> {
    List<Message> findByTag(String tag);
    Message findById(String id);
}
