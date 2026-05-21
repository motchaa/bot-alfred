package com.alfred.bot.infrastructure.adapter.out.persistence;

import com.alfred.bot.domain.model.Category;
import com.alfred.bot.domain.port.out.CategoryRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCategoryRepository implements CategoryRepositoryPort {
    private final SpringDataCategoryRepository repository;

    public JpaCategoryRepository(SpringDataCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Category> findByName(String name) {
        return repository.findByName(name)
                .map(entity -> Category.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .build());
    }

    @Override
    public Category save(Category category) {
        CategoryEntity entity = new CategoryEntity();
        entity.setName(category.getName());

        CategoryEntity saved = repository.save(entity);

        return Category.builder()
                .id(saved.getId())
                .name(saved.getName())
                .build();
    }
}
