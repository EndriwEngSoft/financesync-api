package com.endriw.financesync.service;

import com.endriw.financesync.dto.CategoryRequest;
import com.endriw.financesync.model.Category;
import com.endriw.financesync.model.User;
import com.endriw.financesync.repository.CategoryRepository;
import com.endriw.financesync.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    public static final String NAME = "Alimentação";
    public static final String DESCRIPTION = "Gastos com alimentação";
    public static final String EMAIL = "test@email.com";

    public static final String UPDATED_NAME = "Transporte";
    public static final String UPDATED_DESCRIPTION = "Gastos com transporte";

    @Mock
    CategoryRepository categoryRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    CategoryService categoryService;

    @Test
    void create_whenUserExists_shouldSaveCategory() {
        User user = new User();
        user.setId(1L);

        CategoryRequest  request = new CategoryRequest();

        Category category = new Category();

        category.setName(NAME);
        category.setDescription(DESCRIPTION);
        category.setUser(user);
        category.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.save(any())).thenReturn(category);

        Category response = categoryService.create(request, EMAIL);

        assertNotNull(response);
        assertEquals(NAME, response.getName());
        assertEquals(DESCRIPTION, response.getDescription());
        assertEquals(user, response.getUser());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.getCreatedAt().getDayOfYear());
        verify(categoryRepository, times(1)).save(any());
    }

    @Test
    void create_whenUserDoesNotExists_shouldNotSaveCategory() {
        CategoryRequest  request = new CategoryRequest();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.create(request, EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void findAll_whenUserExists_shouldReturnCategories() {
        User user = new User();

        Category category = new Category();

        category.setName(NAME);
        category.setDescription(DESCRIPTION);
        category.setUser(user);
        category.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findByUser(any())).thenReturn(List.of(category));

        List<Category> response = categoryService.findAll(EMAIL);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(NAME, response.get(0).getName());
        assertEquals(DESCRIPTION, response.get(0).getDescription());
        assertEquals(user, response.get(0).getUser());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.get(0).getCreatedAt().getDayOfYear());
        verify(categoryRepository, times(1)).findByUser(any());
    }

    @Test
    void findAll_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.findAll(EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void findById_whenUserExists_shouldReturnCategory() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();

        category.setName(NAME);
        category.setDescription(DESCRIPTION);
        category.setUser(user);
        category.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(category));

        Category response = categoryService.findById(1L, EMAIL);

        assertNotNull(response);
        assertEquals(NAME, response.getName());
        assertEquals(DESCRIPTION, response.getDescription());
        assertEquals(user, response.getUser());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.getCreatedAt().getDayOfYear());
        verify(categoryRepository, times(1)).findById(any());
    }

    @Test
    void findById_whenUserNotFound_shouldThrowException() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.findById(1L, EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void findById_whenCategoryNotFound_shouldThrowException() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.findById(1L, EMAIL));
        assertEquals("Category not found with id: 1", ex.getMessage());
    }

    @Test
    void findById_whenCategoryBelongsToAnotherUser_shouldThrowException() {
        User user = new User();
        user.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        Category  category = new Category();
        category.setUser(user2);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(category));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.findById(1L, EMAIL));
        assertEquals("Category does not belong to this user: 1", ex.getMessage());
    }

    @Test
    void update_whenUserExists_shouldUpdateCategory() {
        CategoryRequest  request = new CategoryRequest();

        request.setName(UPDATED_NAME);
        request.setDescription(UPDATED_DESCRIPTION);

        User user = new User();
        user.setId(1L);

        Category category = new Category();

        category.setName(NAME);
        category.setDescription(DESCRIPTION);
        category.setUser(user);
        category.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenReturn(category);

        Category response = categoryService.update(1L, request, EMAIL);

        assertNotNull(response);
        verify(categoryRepository, times(1)).findById(any());
        assertEquals(UPDATED_NAME, response.getName());
        assertEquals(UPDATED_DESCRIPTION, response.getDescription());
        assertEquals(user, response.getUser());
        assertEquals(LocalDateTime.now().getDayOfYear(), response.getCreatedAt().getDayOfYear());
    }

    @Test
    void update_whenUserDoesNotExists_shouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.update(1L, new CategoryRequest(), EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }

    @Test
    void delete_whenUserExists_shouldDeleteCategory() {
        User user = new User();
        user.setId(1L);

        Category category = new Category();

        category.setUser(user);
        category.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(category));

        categoryService.delete(1L, EMAIL);

        verify(categoryRepository, times(1)).findById(any());
        verify(userRepository, times(1)).findByEmail(any());
        verify(categoryRepository, times(1)).delete(any());
    }

    @Test
    void delete_whenUserDoesNotExists_shouldThrowException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoryService.delete(1L, EMAIL));
        assertEquals("User not found: " + EMAIL, ex.getMessage());
    }
}