package com.endriw.financesync.controller;

import com.endriw.financesync.dto.CategoryRequest;
import com.endriw.financesync.model.Category;
import com.endriw.financesync.model.User;
import com.endriw.financesync.security.CustomUserDetailsService;
import com.endriw.financesync.security.JwtService;
import com.endriw.financesync.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    public static final String CATEGORY_NAME = "Category 1";
    public static final String CATEGORY_DESCRIPTION = "Description 1";
    public static final LocalDateTime CREATED_AT = LocalDateTime.now();
    public static final String EMAIL = "test@email.com";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CategoryService categoryService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    private User user;
    private Category category;

    private CategoryRequest buildValidRequest() {

        CategoryRequest request = new CategoryRequest();

        request.setName(CATEGORY_NAME);
        request.setDescription(CATEGORY_DESCRIPTION);

        return request;
    }

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        category = new Category();

        category.setName(CATEGORY_NAME);
        category.setDescription(CATEGORY_DESCRIPTION);
        category.setCreatedAt(CREATED_AT);
        category.setUser(user);
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findAll_whenCategoryExist_ShouldReturnListAndOk() throws Exception {

        when(categoryService.findAll(anyString())).thenReturn(List.of(category));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$[0].description").value(CATEGORY_DESCRIPTION))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findAll_whenNoCategoryExist_shouldReturnEmptyListAndOk() throws Exception {

        when(categoryService.findAll(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findAll_whenUserNotFound_shouldReturnNotFound() throws Exception {

        when(categoryService.findAll(anyString())).thenThrow(
                new RuntimeException("User not found: " + EMAIL));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found: " + EMAIL))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void findAll_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findById_whenCategoryExists_shouldReturnCategoryAndOk() throws Exception {

        when(categoryService.findById(anyLong(), anyString())).thenReturn(category);

        mockMvc.perform(get("/categories/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.description").value(CATEGORY_DESCRIPTION))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findById_whenCategoryNotFound_shouldReturnNotFound() throws Exception {

        when(categoryService.findById(anyLong(), anyString())).thenThrow(
                new RuntimeException("Category not found with id: 1"));

        mockMvc.perform(get("/categories/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found with id: " + 1L))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void findById_whenCategoryBelongsToAnotherUser_shouldReturnForbidden() throws Exception {

        when(categoryService.findById(anyLong(), anyString())).thenThrow(
                new RuntimeException("Category does not belong to this user: 1")
        );

        mockMvc.perform(get("/categories/{id}", 1L))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Category does not belong to this user: 1"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void findById_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/categories/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void createCategory_whenRequestIsValid_shouldReturnCreated() throws Exception {

        CategoryRequest request = buildValidRequest();

        when(categoryService.create(any(CategoryRequest.class), anyString())).thenReturn(category);

        mockMvc.perform(post("/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.description").value(CATEGORY_DESCRIPTION))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void createCategory_whenRequestIsInvalid_shouldReturnBadRequest() throws Exception {

        CategoryRequest request = new CategoryRequest();

        request.setName("");
        request.setDescription(CATEGORY_DESCRIPTION);

        mockMvc.perform(post("/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("name: must not be blank"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void createCategory_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(post("/categories")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void updateCategory_whenRequestIsValid_shouldReturnUpdated() throws Exception {

        CategoryRequest request = new CategoryRequest();

        request.setName(CATEGORY_NAME);
        request.setDescription(CATEGORY_DESCRIPTION);

        Category updatedCategory = new Category();

        updatedCategory.setName(CATEGORY_NAME);
        updatedCategory.setDescription(CATEGORY_DESCRIPTION);
        updatedCategory.setCreatedAt(CREATED_AT);

        when(categoryService.update(anyLong(),any(CategoryRequest.class),anyString())).thenReturn(updatedCategory);

        mockMvc.perform(put("/categories/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(CATEGORY_NAME))
                .andExpect(jsonPath("$.description").value(CATEGORY_DESCRIPTION))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void updateCategory_whenRequestIsInvalid_shouldReturnBadRequest() throws Exception {

        CategoryRequest request = new CategoryRequest();

        request.setName("");
        request.setDescription(CATEGORY_DESCRIPTION);

        mockMvc.perform(put("/categories/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("name: must not be blank"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void updateCategory_whenCategoryNotFound_shouldReturnNotFound() throws Exception {

        CategoryRequest request = new CategoryRequest();

        request.setName(CATEGORY_NAME);
        request.setDescription(CATEGORY_DESCRIPTION);

        when(categoryService.update(anyLong(),any(CategoryRequest.class),anyString())).thenThrow(
                new RuntimeException("Category not found with id: 1")
        );

        mockMvc.perform(put("/categories/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found with id: " + 1L))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void updateCategory_whenCategoryBelongsToAnotherUser_shouldReturnForbidden() throws Exception {

        CategoryRequest request = new CategoryRequest();

        request.setName(CATEGORY_NAME);
        request.setDescription(CATEGORY_DESCRIPTION);

        when(categoryService.update(anyLong(),any(CategoryRequest.class),anyString())).thenThrow(
                new RuntimeException("Category does not belong to this user: 1")
        );

        mockMvc.perform(put("/categories/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Category does not belong to this user: " + 1L))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void updateCategory_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(put("/categories/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void deleteCategory_whenCategoryExists_shouldReturnNoContent() throws Exception {

        doNothing().when(categoryService).delete(anyLong(), anyString());

        mockMvc.perform(delete("/categories/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void deleteCategory_whenCategoryNotFound_shouldReturnNotFound() throws Exception {

        doThrow(new RuntimeException("Category not found with id: 1")).
                when(categoryService).delete(anyLong(), anyString());

        mockMvc.perform(delete("/categories/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category not found with id: " + 1L))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL)
    void deleteCategory_whenCategoryBelongsToAnotherUser_shouldReturnForbidden() throws Exception {

        doThrow(new RuntimeException("Category does not belong to this user: 1")).
                when(categoryService).delete(anyLong(), anyString());

        mockMvc.perform(delete("/categories/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Category does not belong to this user: " + 1L))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void deleteCategory_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {

        mockMvc.perform(delete("/categories/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}