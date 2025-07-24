package com.example.resellkh.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductFile {
    private Long id;
    private Long productId;
    private String fileUrl;
    private String contentType;
}