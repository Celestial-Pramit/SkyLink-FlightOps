package com.skylink.app.service.impl;

import com.skylink.app.entity.Customer;
import com.skylink.app.exception.BusinessRuleException;
import com.skylink.app.exception.ResourceNotFoundException;
import com.skylink.app.repository.BookingRepository;
import com.skylink.app.repository.CustomerRepository;
import com.skylink.app.service.ICustomerService;
import com.skylink.app.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements ICustomerService {

    private static final String UPLOAD_SUBFOLDER = "customers";

    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final IFileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        return customerRepository.search(query.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email) {
        return !customerRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email, Long excludeId) {
        return !customerRepository.existsByEmailAndIdNot(email, excludeId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNidUnique(String nid) {
        return !customerRepository.existsByNidOrPassport(nid);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNidUnique(String nid, Long excludeId) {
        return !customerRepository.existsByNidOrPassportAndIdNot(nid, excludeId);
    }

    @Override
    public Customer save(Customer customer, MultipartFile photo) throws IOException {
        validateUniqueFields(customer.getEmail(), customer.getNidOrPassport(), null);

        if (photo != null && !photo.isEmpty()) {
            customer.setPhotoPath(fileStorageService.store(photo, UPLOAD_SUBFOLDER));
        }

        Customer saved = customerRepository.save(customer);
        log.info("Created customer: {} ({})", saved.getFullName(), saved.getEmail());
        return saved;
    }

    @Override
    public Customer update(Long id, Customer updatedData, MultipartFile photo) throws IOException {
        Customer existing = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        validateUniqueFields(updatedData.getEmail(), updatedData.getNidOrPassport(), id);

        existing.setFullName(updatedData.getFullName());
        existing.setEmail(updatedData.getEmail());
        existing.setPhone(updatedData.getPhone());
        existing.setNidOrPassport(updatedData.getNidOrPassport());
        existing.setDateOfBirth(updatedData.getDateOfBirth());
        existing.setAddress(updatedData.getAddress());

        if (photo != null && !photo.isEmpty()) {
            String newPhotoPath = fileStorageService.store(photo, UPLOAD_SUBFOLDER);
            deleteStoredPhoto(existing.getPhotoPath());
            existing.setPhotoPath(newPhotoPath);
        }

        Customer saved = customerRepository.save(existing);
        log.info("Updated customer: {}", saved.getEmail());
        return saved;
    }

    @Override
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        long bookingCount = bookingRepository.findByCustomer(customer).size();
        if (bookingCount > 0) {
            throw new BusinessRuleException(
                "Cannot delete customer '" + customer.getFullName() +
                "' - they have " + bookingCount + " existing booking(s). " +
                "Cancel all bookings before deleting the customer.");
        }

        deleteStoredPhoto(customer.getPhotoPath());
        customerRepository.delete(customer);
        log.info("Deleted customer: {}", customer.getFullName());
    }

    private void validateUniqueFields(String email, String nidOrPassport, Long excludeId) {
        boolean duplicateEmail = excludeId == null
            ? !isEmailUnique(email)
            : !isEmailUnique(email, excludeId);
        if (duplicateEmail) {
            throw new BusinessRuleException(
                "Email '" + email + "' is already registered to another customer.");
        }

        boolean duplicateNid = excludeId == null
            ? !isNidUnique(nidOrPassport)
            : !isNidUnique(nidOrPassport, excludeId);
        if (duplicateNid) {
            throw new BusinessRuleException(
                "NID/Passport '" + nidOrPassport +
                "' is already registered to another customer.");
        }
    }

    private void deleteStoredPhoto(String photoPath) {
        if (photoPath == null || photoPath.isBlank()) {
            return;
        }
        int slash = photoPath.lastIndexOf('/');
        String filename = slash >= 0 ? photoPath.substring(slash + 1) : photoPath;
        fileStorageService.delete(filename, UPLOAD_SUBFOLDER);
    }
}
