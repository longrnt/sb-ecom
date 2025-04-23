package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

//    private List<Category> categories = new ArrayList<>();

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new APIException("No Category Found");
        }

        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        return new CategoryResponse(categoryDTOS);
    }

    @Override
    public void createCategory(Category category) {
        Category savedCategory = categoryRepository.getCategoryByCategoryName(category.getCategoryName());
        if (savedCategory != null) {
            throw new APIException("Category already exists");
        }
        categoryRepository.save(category);
        System.out.println("Category added successfully");
    }

    @Override
    public String deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Category with id %s not found", id)));
        .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", id));
        categoryRepository.delete(category);
        return "Category with categoryId " + id + " deleted successfully";

    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {

        Category categoryUpdated = categoryRepository.findById(categoryId).
            orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

//        Optional<Category> categoryOptional = categories.stream()
//                .filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst();

        categoryUpdated.setCategoryName(category.getCategoryName());
        categoryRepository.save(categoryUpdated);
        return categoryUpdated;
    }
}
