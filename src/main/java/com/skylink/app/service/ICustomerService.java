package com.skylink.app.service;

import com.skylink.app.entity.Customer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ICustomerService {
    List<Customer> findAll();
    Optional<Customer> findById(Long id);
    List<Customer> search(String query);
    Customer save(Customer customer, MultipartFile photo) throws IOException;
    Customer update(Long id, Customer customer, MultipartFile photo) throws IOException;
    void delete(Long id);
    boolean isEmailUnique(String email);
    boolean isEmailUnique(String email, Long excludeId);
    boolean isNidUnique(String nid);
    boolean isNidUnique(String nid, Long excludeId);
}
