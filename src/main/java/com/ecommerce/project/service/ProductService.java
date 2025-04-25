package com.ecommerce.project.service;


import com.ecommerce.project.payload.ProductDTO;

public interface ProductService {
    ProductDTO addProduct(Long categoryId, ProductDTO productDTO);
}
