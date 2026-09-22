package com.dextime.api.controller;

import com.dextime.api.dto.UserDto;
import com.dextime.api.entity.UserEntity;
import com.dextime.api.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /*
     * Customer operations include requesting appointments and creating a customer account.
     */

    @PostMapping
    public ResponseEntity<UserEntity> createCustomer(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(customerService.createCustomer(userDto));
    }

}
