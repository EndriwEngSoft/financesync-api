package com.endriw.financesync.controller;

import com.endriw.financesync.dto.CategoryRequest;
import com.endriw.financesync.model.Category;
import com.endriw.financesync.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> findAll(Principal principal) {
        return ResponseEntity.ok(categoryService.findAll(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> findById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(categoryService.findById(id, principal.getName()));
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody CategoryRequest category, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(category, principal.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                   @RequestBody CategoryRequest category,
                                                   Principal principal) {
        return ResponseEntity.ok(categoryService.update(id, category, principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  deleteCategory(@PathVariable Long id, Principal principal) {
        categoryService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

}
