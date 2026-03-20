package com.endriw.financesync.service;

import com.endriw.financesync.dto.CategoryRequest;
import com.endriw.financesync.model.Category;
import com.endriw.financesync.model.User;
import com.endriw.financesync.repository.CategoryRepository;
import com.endriw.financesync.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository,  UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public Category create(CategoryRequest request, String email) {
        User user = getAuthenticatedUser(email);
        Category category = new Category();
        category.setUser(user);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setCreatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    public List<Category> findAll(String email) {
        User user = getAuthenticatedUser(email);
        return categoryRepository.findByUser(user);
    }

    public Category findById(Long id, String email) {
        User user = getAuthenticatedUser(email);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category does not belong to this user: " + id));

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Category does not belong to this user: " + id);
        }

        return category;
    }

    public Category update(Long id, CategoryRequest request, String email) {
        Category updateCategory = findById(id, email);
        updateCategory.setName(request.getName());
        updateCategory.setDescription(request.getDescription());
        return categoryRepository.save(updateCategory);
    }

    public void delete(Long id, String email) {
        Category category = findById(id, email);
        categoryRepository.delete(category);
    }

    private User getAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

}
