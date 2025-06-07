package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ContactInfoRequest {
    private int userId;
    private String telegramUrl;
}
