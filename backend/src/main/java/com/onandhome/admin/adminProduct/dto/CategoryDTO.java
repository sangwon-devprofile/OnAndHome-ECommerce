package com.onandhome.admin.adminProduct.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private String parentCategory;
    private String parentCategoryName;
    private List<String> subCategories;
}
