package com.ecommerce.project.service;

import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {

        Product product = modelMapper.map(productDTO, Product.class);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        product.setImage("default.png");
        product.setCategory(category);
        double specialPrice = product.getPrice() - (productDTO.getPrice() * product.getDiscount() * 0.01);
        product.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts() {
        List<Product> productList = productRepository.findAll();
        List<ProductDTO> productDTOList = productList.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        return new ProductResponse(productDTOList);
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        List<Product> productList = productRepository.findByCategoryOrderByPriceAsc(category);
        List<ProductDTO> productDTOList = productList.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        return new ProductResponse(productDTOList);
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword) {
        List<Product> productList = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%');
        List<ProductDTO> productDTOList = productList.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        return new ProductResponse(productDTOList);
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        productFromDb.setProductName(productDTO.getProductName());
        productFromDb.setImage(productDTO.getImage());
        productFromDb.setDescription(productDTO.getDescription());
        productFromDb.setQuantity(productDTO.getQuantity());
        productFromDb.setPrice(productDTO.getPrice());
        productFromDb.setDiscount(productDTO.getDiscount());

        productFromDb.setSpecialPrice(productDTO.getSpecialPrice());

        Product savedProduct = productRepository.save(productFromDb);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        productRepository.delete(productFromDb);

        return modelMapper.map(productFromDb, ProductDTO.class);

    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        // Get the product from DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Upload image to server
        // Get the file name of uploaded image
        String path = "images/";
        String fileName = uploadImage(path, image);

        // Updating the new file name to the product
        productFromDb.setImage(fileName);

        // Save updated product
        Product updatedProduct = productRepository.save(productFromDb);

        // return DTO after mapping product to DTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    private String uploadImage(String path, MultipartFile file) throws IOException {
        // File names of current / original file
        String originalFilename = file.getOriginalFilename();

        // Generate a unique file name
        String randomId = UUID.randomUUID().toString();
        // mat.jpg --> 1234 ---> 1234.jpg
        String fileName = randomId.concat(originalFilename.substring(originalFilename.lastIndexOf(".")));
        // File.pathSeparator will help this app to be able to run in multiple OS
        String filePath = path + File.separator + fileName;

        // Check if path exist and create
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Upload to server
        Files.copy(file.getInputStream(), Paths.get(filePath));

        // Returning file name
        return fileName;

    }

}
