package com.skylink.app.repository;

import com.skylink.app.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.nidOrPassport = :nidOrPassport")
    boolean existsByNidOrPassport(String nidOrPassport);

    @Query("SELECT COUNT(c) > 0 FROM Customer c " +
           "WHERE c.nidOrPassport = :nidOrPassport AND c.id <> :id")
    boolean existsByNidOrPassportAndIdNot(
        @Param("nidOrPassport") String nidOrPassport,
        @Param("id") Long id
    );

    @Query("""
        SELECT c FROM Customer c
        WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%'))
        OR c.phone LIKE CONCAT('%', :query, '%')
    """)
    List<Customer> search(@Param("query") String query);
}
