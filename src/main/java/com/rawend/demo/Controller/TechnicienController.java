package com.rawend.demo.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/Technicien")
public class TechnicienController {
	 @GetMapping
	    public ResponseEntity<String> sayHi() {
	        return ResponseEntity.ok("Hi Techniciennnnnn");
	    }

}
