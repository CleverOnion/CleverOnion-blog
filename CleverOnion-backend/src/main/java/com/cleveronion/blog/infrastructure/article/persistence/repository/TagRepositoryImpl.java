package com.cleveronion.blog.infrastructure.article.persistence.repository;

import com.cleveronion.blog.domain.article.aggregate.TagAggregate;
import com.cleveronion.blog.domain.article.repository.TagRepository;
import com.cleveronion.blog.domain.article.valueobject.TagId;
import com.cleveronion.blog.infrastructure.article.persistence.converter.TagConverter;
import com.cleveronion.blog.infrastructure.article.persistence.po.TagPO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签仓储实现类
 * 实现TagRepository接口，桥接领域层和基础设施层
 * 
 * @author CleverOnion
 */
@Repository
public class TagRepositoryImpl implements TagRepository {
    
    private final TagJpaRepository tagJpaRepository;
    
    public TagRepositoryImpl(TagJpaRepository tagJpaRepository) {
        this.tagJpaRepository = tagJpaRepository;
    }
    
    @Override
    public TagAggregate save(TagAggregate tag) {
        TagPO tagPO = TagConverter.toTagPO(tag);
        TagPO savedTagPO = tagJpaRepository.save(tagPO);
        return TagConverter.toTagAggregate(savedTagPO);
    }
    
    @Override
    public Optional<TagAggregate> findById(TagId id) {
        Optional<TagPO> tagPOOpt = tagJpaRepository.findById(id.getValue());
        return tagPOOpt.map(TagConverter::toTagAggregate);
    }
    
    @Override
    public Optional<TagAggregate> findByName(String name) {
        Optional<TagPO> tagPOOpt = tagJpaRepository.findByName(name);
        return tagPOOpt.map(TagConverter::toTagAggregate);
    }
    
    @Override
    public List<TagAggregate> findByIds(Set<TagId> ids) {
        Set<Long> idValues = ids.stream()
            .map(TagId::getValue)
            .collect(Collectors.toSet());
        List<TagPO> tagPOs = tagJpaRepository.findByIdIn(idValues);
        return tagPOs.stream()
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TagAggregate> findByNames(Set<String> names) {
        List<TagPO> tagPOs = tagJpaRepository.findByNameIn(names);
        return tagPOs.stream()
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(TagId id) {
        tagJpaRepository.deleteById(id.getValue());
    }
    
    @Override
    public boolean existsById(TagId id) {
        return tagJpaRepository.existsById(id.getValue());
    }
    
    @Override
    public boolean existsByName(String name) {
        return tagJpaRepository.existsByName(name);
    }
    
    @Override
    public List<TagAggregate> findAll() {
        List<TagPO> tagPOs = tagJpaRepository.findAllByOrderByNameAsc();
        return tagPOs.stream()
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TagAggregate> findByNameContaining(String keyword) {
        List<TagPO> tagPOs = tagJpaRepository.findByNameContainingIgnoreCase(keyword);
        return tagPOs.stream()
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TagAggregate> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<TagPO> tagPOs = tagJpaRepository.findAllByOrderByCreatedAtDesc(pageable);
        return tagPOs.stream()
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        return tagJpaRepository.count();
    }
    
    @Override
    public List<TagAggregate> findRecentlyCreated(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<TagPO> tagPOs = tagJpaRepository.findRecentlyCreated(pageable);
        return tagPOs.stream()
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TagAggregate> findPopularTags(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<TagPO> tagPOs = tagJpaRepository.findPopularTags(pageable);
        return tagPOs.stream()
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TagAggregate> findByNameStartingWith(String prefix) {
        List<TagPO> tagPOs = tagJpaRepository.findByNameStartingWithIgnoreCase(prefix);
        return tagPOs.stream()
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TagAggregate> findAllOrderByName(boolean ascending) {
        List<TagPO> tagPOs = ascending ? 
            tagJpaRepository.findAllByOrderByNameAsc() : 
            tagJpaRepository.findAllByOrderByNameDesc();
        return tagPOs.stream()
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TagAggregate> findAllOrderByCreatedAt(boolean ascending) {
        List<TagPO> tagPOs = ascending ? 
            tagJpaRepository.findAllByOrderByCreatedAtAsc() : 
            tagJpaRepository.findAllByOrderByCreatedAtDesc();
        return tagPOs.stream()
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TagAggregate> findUnusedTags() {
        // 查找没有被任何文章使用的标签
        List<TagPO> allTags = tagJpaRepository.findAll();
        return allTags.stream()
            .filter(tagPO -> tagJpaRepository.countUsageByTagId(tagPO.getId()) == 0)
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TagAggregate> saveAll(List<TagAggregate> tags) {
        List<TagPO> tagPOs = tags.stream()
            .map(TagConverter::toTagPO)
            .collect(Collectors.toList());
        List<TagPO> savedTagPOs = tagJpaRepository.saveAll(tagPOs);
        return savedTagPOs.stream()
            .map(TagConverter::toTagAggregate)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteByIds(Set<TagId> ids) {
        Set<Long> idValues = ids.stream()
            .map(TagId::getValue)
            .collect(Collectors.toSet());
        tagJpaRepository.deleteAllById(idValues);
    }
    
    @Override
    public List<TagWithArticleCount> findTagsWithArticleCount(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Object[]> results = tagJpaRepository.findTagsWithArticleCount(pageable);
        
        return results.stream()
            .map(result -> {
                TagPO tagPO = (TagPO) result[0];
                Long articleCount = (Long) result[1];
                TagAggregate tagAggregate = TagConverter.toTagAggregate(tagPO);
                return new TagWithArticleCount(tagAggregate, articleCount);
            })
            .collect(Collectors.toList());
    }
}