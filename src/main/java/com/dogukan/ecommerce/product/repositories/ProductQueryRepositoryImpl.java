package com.dogukan.ecommerce.product.repositories;

import com.dogukan.ecommerce.product.dtos.ProductFilter;
import com.dogukan.ecommerce.product.entities.Product;
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
public class ProductQueryRepositoryImpl implements  ProductQueryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Product> search(ProductFilter filter, Pageable pageable) {
        StringBuilder jpql=new StringBuilder("""
                select p from Product p
                 left join fetch p.category cat
                 left join fetch p.createdBy cb 
                 where p.deleted = false
                 """);
        StringBuilder countJpql=new StringBuilder("""
                select count(p) from Product p
                 where p.deleted = false
                 """);

        List<String> where = new ArrayList<>();

        if (filter != null) {
            if (StringUtils.hasText(filter.q())) {

                where.add("""
                    (
                      lower(p.name) like :q
                      or lower(p.description) like :q
                    )
                """);
            }
            if (filter.categoryId() != null) {
                where.add("p.category.id = :categoryId");
            }
            if (filter.active() != null) {
                where.add("p.active = :active");
            }
            if (filter.minPrice() != null) {
                where.add("p.price >= :minPrice");
            }
            if (filter.maxPrice() != null) {
                where.add("p.price <= :maxPrice");
            }
            if (filter.createdById() != null) {
                where.add("p.createdBy.id = :createdById");
            }
        }

        for (String w : where) {
            jpql.append(" and ").append(w);
            countJpql.append(" and ").append(w);
        }

        jpql.append(" order by p.id desc");

        TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);

        if (filter != null) {
            if (StringUtils.hasText(filter.q())) {
                String q = "%" + filter.q().toLowerCase().trim() + "%";
                query.setParameter("q", q);
                countQuery.setParameter("q", q);
            }
            if (filter.categoryId() != null) {
                query.setParameter("categoryId", filter.categoryId());
                countQuery.setParameter("categoryId", filter.categoryId());
            }
            if (filter.active() != null) {
                query.setParameter("active", filter.active());
                countQuery.setParameter("active", filter.active());
            }
            if (filter.minPrice() != null) {
                query.setParameter("minPrice", filter.minPrice());
                countQuery.setParameter("minPrice", filter.minPrice());
            }
            if (filter.maxPrice() != null) {
                query.setParameter("maxPrice", filter.maxPrice());
                countQuery.setParameter("maxPrice", filter.maxPrice());
            }
            if (filter.createdById() != null) {
                query.setParameter("createdById", filter.createdById());
                countQuery.setParameter("createdById", filter.createdById());
            }
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        long total = countQuery.getSingleResult();
        List<Product> content = query.getResultList();

        return new PageImpl<>(content, pageable, total);
    }
}
