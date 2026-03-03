package com.dogukan.ecommerce.order.repositories;

import com.dogukan.ecommerce.order.dtos.OrderFilter;
import com.dogukan.ecommerce.order.entities.Order;
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
public class OrderQueryRepositoryImpl implements OrderQueryRepository{

    @PersistenceContext
    private EntityManager em;


    @Override
    public Page<Order> search(OrderFilter filter, Pageable pageable) {

        StringBuilder jpql=new StringBuilder("""
                select o from Order o
                left join fetch o.customer c
                where o.deleted = false
                """);

        StringBuilder countJpql=new StringBuilder("""
                select count(o) from Order o
                where o.deleted = false
                """);

        List<String> where = new ArrayList<>();

        if(filter!=null){
            if(StringUtils.hasText(filter.q())){
                where.add("lower(o.orderNumber) like :q");
            }
            if (filter.status() != null) {
                where.add("o.status = :status");
            }
            if (filter.customerId() != null) {
                where.add("o.customer.id = :customerId");
            }
            if (filter.minTotal() != null) {
                where.add("o.totalAmount >= :minTotal");
            }
            if (filter.maxTotal() != null) {
                where.add("o.totalAmount <= :maxTotal");
            }
            if (filter.createdFrom() != null) {
                where.add("o.createdAt >= :createdFrom");
            }
            if (filter.createdTo() != null) {
                where.add("o.createdAt <= :createdTo");
            }
        }
        for (String w : where) {
            jpql.append(" and ").append(w);
            countJpql.append(" and ").append(w);
        }

        jpql.append(" order by o.id desc");
        TypedQuery<Order> query=em.createQuery(jpql.toString(),Order.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql.toString(), Long.class);


        if (filter != null) {
            if (StringUtils.hasText(filter.q())) {
                String q = "%" + filter.q().toLowerCase().trim() + "%";
                query.setParameter("q", q);
                countQuery.setParameter("q", q);
            }
            if (filter.status() != null) {
                query.setParameter("status", filter.status());
                countQuery.setParameter("status", filter.status());
            }
            if (filter.customerId() != null) {
                query.setParameter("customerId", filter.customerId());
                countQuery.setParameter("customerId", filter.customerId());
            }
            if (filter.minTotal() != null) {
                query.setParameter("minTotal", filter.minTotal());
                countQuery.setParameter("minTotal", filter.minTotal());
            }
            if (filter.maxTotal() != null) {
                query.setParameter("maxTotal", filter.maxTotal());
                countQuery.setParameter("maxTotal", filter.maxTotal());
            }
            if (filter.createdFrom() != null) {
                query.setParameter("createdFrom", filter.createdFrom());
                countQuery.setParameter("createdFrom", filter.createdFrom());
            }
            if (filter.createdTo() != null) {
                query.setParameter("createdTo", filter.createdTo());
                countQuery.setParameter("createdTo", filter.createdTo());
            }
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        long total = countQuery.getSingleResult();
        List<Order> content = query.getResultList();

        return new PageImpl<>(content, pageable, total);
    }
}
