package com.example.resellkh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private int id;
    private int userId;
    private String content;
    private LocalDateTime createdAt;
}
