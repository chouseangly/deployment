package com.example.resellkh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContactInfo {
    private int id;
    private int userId;
    private String telegramUrl;
}
