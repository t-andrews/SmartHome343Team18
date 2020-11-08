package com.smart.home.backend.controller;

import com.smart.home.backend.service.OutputConsole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Smart Home Security Controller
 */
@Getter
@Setter
@RestController
public class OutputConsoleController {
	
	/**
	 * Retrieving the console lines.
	 * @return The console lines
	 */
	@GetMapping("console/lines")
	public ResponseEntity<List<String>> getAwayMode(){
		return new ResponseEntity<>(OutputConsole.getLines(), HttpStatus.OK);
	}
	
	/**
	 * Retrieving the console lines.
	 * @return The console lines
	 */
	@PostMapping("console/lines")
	public ResponseEntity<List<String>> logLine(@RequestBody String line) {
		OutputConsole.log(line);
		return new ResponseEntity<>(OutputConsole.getLines(), HttpStatus.OK);
	}
	
}
