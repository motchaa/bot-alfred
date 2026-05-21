package com.alfred.bot.domain.port.out;

import com.alfred.bot.domain.model.Category;

import java.util.Optional;

public interface CategoryRepositoryPort {
    Optional<Category> findByName(String name);
    Category save(Category category);
}
