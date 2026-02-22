package com.dogukan.ecommerce.user.repositories;

import com.dogukan.ecommerce.user.dtos.UserFilter;
import com.dogukan.ecommerce.user.entities.User;
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
public class UserQueryRepositoryImpl implements UserQueryRepository {

    @PersistenceContext
    private EntityManager em;


    @Override
    public Page<User> searchUsers(UserFilter filter, Pageable pageable) {
        StringBuilder jpql=new StringBuilder("select u from User u where 1=1");
        StringBuilder countJpql=new StringBuilder("select count(u) from User u where 1=1");

        List<String> where=new ArrayList<>();
        if(filter!=null){
            if(StringUtils.hasText(filter.q())) {
                where.add("(lower(u.username) like :q or lower(u.email) like q: or (lower(u.firstName) like :q or lower(u.lastName) like :q)");
            }
            if(filter.role()!=null){
                where.add(":role MEMBER OF u.roles");
            }
            if(filter.enabled()!=null){
                where.add("u.enables = :enabled");
            }
        }
        for (String  w:where){
            jpql.append(" and ").append(w);
            countJpql.append(" and ").append(w);
        }

        jpql.append(" order by u.id desc");
        TypedQuery<User> query=em.createQuery(jpql.toString(),User.class);
        TypedQuery<Long> countQuery=em.createQuery(countJpql.toString(),Long.class);

        if(filter!=null){
            if (StringUtils.hasText(filter.q())) {
                String q = "%" + filter.q().toLowerCase().trim() + "%";
                query.setParameter("q", q);
                countQuery.setParameter("q", q);
            }
            if (filter.role() != null) {
                query.setParameter("role", filter.role());
                countQuery.setParameter("role", filter.role());
            }
            if (filter.enabled() != null) {
                query.setParameter("enabled", filter.enabled());
                countQuery.setParameter("enabled", filter.enabled());
            } if (StringUtils.hasText(filter.q())) {
                String q = "%" + filter.q().toLowerCase().trim() + "%";
                query.setParameter("q", q);
                countQuery.setParameter("q", q);
            }
            if (filter.role() != null) {
                query.setParameter("role", filter.role());
                countQuery.setParameter("role", filter.role());
            }
            if (filter.enabled() != null) {
                query.setParameter("enabled", filter.enabled());
                countQuery.setParameter("enabled", filter.enabled());
            }
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        long total=countQuery.getSingleResult();
        List<User> content=query.getResultList();
        return new PageImpl<>(content,pageable,total);
    }
}
