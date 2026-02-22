package com.dogukan.ecommerce.category.repositories;

import com.dogukan.ecommerce.category.dtos.CategoryFilter;
import com.dogukan.ecommerce.category.entities.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CategoryQueryRepositoryImpl implements CategoryQueryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Category> search(CategoryFilter filter, Pageable pageable) {
        StringBuilder jpql=new StringBuilder("select c from Category c left join fetch c.createdBy cb where c.deleted = false");
        StringBuilder countJpql=new StringBuilder("select count(c) from Category c where c.deleted = false");

        List<String> where=new ArrayList<>();
        if(filter!=null){
            if(StringUtils.hasText(filter.q())){
                where.add("""
                        (
                        lower(c.name) like :q
                        )
                        """);
            }
            if(filter.active()!=null){
                where.add("""
                         c.active = :active
                        """);

            }
        }

        for (String w:where){
            jpql.append(" and ").append(w);
            countJpql.append(" and ").append(w);
        }
        jpql.append(" order by c.id desc");
        TypedQuery<Category> query=em.createQuery(jpql.toString(),Category.class);
        TypedQuery<Long> countQuery=em.createQuery(countJpql.toString(),Long.class);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        long total = countQuery.getSingleResult();
        List<Category> content = query.getResultList();

        return new PageImpl<>(content, pageable, total);
    }

}
