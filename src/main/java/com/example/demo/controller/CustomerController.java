package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.CustomerDto;
import com.example.demo.model.Company;
import com.example.demo.model.Customer;
import com.example.demo.model.Person;
import com.example.demo.response.GeocoderResponse;
import com.example.demo.service.CustomerService;

import exception.CustomerNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.parser.JSONParser;
//import org.json.parser.ParseException;

@RestController
public class CustomerController {
	private CustomerService customerService;
	private RestTemplate restTemplate;
	private String geoCodeAPIBaseURL = "https://geocoder.ca/?addresst=edison+drive&stno=1&city=&prov=ON&postal=08520&geoit=GeoCode+it%21";

	public CustomerController(CustomerService customerService, RestTemplate restTemplate) {
		super();
		this.customerService = customerService;
		this.restTemplate = restTemplate;
	}

	@PostMapping("/customer")
	@Operation(summary = "Creates new customer")
	@ApiResponse(responseCode = "201", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	public ResponseEntity<Customer> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
		if (customerDto.getAddress().getCity() == null || customerDto.getAddress().getProvidence() == null) {
			String url = String.format("https://geocoder.ca/?locate=%s&geoit=XML&json=1",
					customerDto.getAddress().getPostalCode());
			String response = restTemplate.getForObject(url, String.class);
			JSONObject jsonResponse = new JSONObject(response);
			JSONObject standard = jsonResponse.getJSONObject("standard");
		//	System.out.println(customer); //customer param has address object in it
			System.out.println(standard);
			System.out.println(jsonResponse);

			customerDto.getAddress().setCity(standard.getString("city")); 
			customerDto.getAddress().setProvidence(standard.getString("prov"));

		}
		Customer createdCustomer = customerService.createCustomer(customerDto); // takes care of the changing the address.customer field to curr cust
		return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
	}

	@GetMapping("/customers")
	@Operation(summary = "Retrieve all customers")
	@ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	public ResponseEntity<List<Customer>> getAllEmployees() {
		return ResponseEntity.ok(customerService.findAllCustomers());
	}

	@GetMapping("/customer/{id}")
	@Operation(summary = "Retrieve Customer by ID")
	@ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	public ResponseEntity<Customer> getCustomerById(@PathVariable long id) {
		Customer customer = null;
		try {
			customer = customerService.findById(id);
		} catch (CustomerNotFoundException e) {
			e.printStackTrace();
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(customer);
	}

	@PutMapping("/customer/{id}")
	@Operation(summary = "Update customer information")
	@ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
	public ResponseEntity<Customer> updateCustomer(@PathVariable long id, @Valid @RequestBody CustomerDto customerDto) {
        try {
            Customer updatedCustomer = customerService.updateCustomer(id, customerDto);
            return ResponseEntity.ok(updatedCustomer);
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

	@DeleteMapping("/customer/{id}")
	@Operation(summary = "Delete customer by id")
	public ResponseEntity<Void> deleteCustomerbyId(@PathVariable long id) {

		try {
			customerService.deleteById(id);
		} catch (CustomerNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}
	@GetMapping("/persons")
	@Operation(summary = "Gets all the Person type Customers'")
	public ResponseEntity<List<Person>> getAllPersonCustomers() {
		return ResponseEntity.ok(customerService.getAllPersons());
	}
	@GetMapping("/companies")
	@Operation(summary = "Gets all the Company type Customers")
	public ResponseEntity<List<Company>> getAllCompanyCustoers() {
		return ResponseEntity.ok(customerService.getAllCompanies());
	}
}
