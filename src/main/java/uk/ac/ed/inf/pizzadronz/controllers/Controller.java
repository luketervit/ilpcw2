package uk.ac.ed.inf.pizzadronz.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.inf.pizzadronz.models.*;
import uk.ac.ed.inf.pizzadronz.service.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class Controller {

    @Autowired
    private OrderValidationService validationService;

    @GetMapping("/uuid")
    public String uuid() {
        return "s2359358";
    }

    @PostMapping("/distanceTo")
    public ResponseEntity<Double> calculateDistance(@RequestBody LngLatPairRequest request) {
        try {
            double distance = request.calculateDistance();
            return ResponseEntity.ok(distance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody LngLatPairRequest request) {
        try {
            // Call the isCloseTo method on the request object
            boolean result = request.isCloseTo();
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @PostMapping("/nextPosition")
    public ResponseEntity<NextPositionRequest.Position> nextPosition(@RequestBody NextPositionRequest request) {
        try {
            NextPositionRequest.Position nextPosition = request.calculateNextPosition();
            return ResponseEntity.ok(nextPosition);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody LngLatPairRequest request) {
        try {
            LngLat point = new LngLat(request.getPosition().getLng(), request.getPosition().getLat());
            Region region = request.getRegion();

            boolean isInside = region.isInRegion(point);
            return ResponseEntity.ok(isInside);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/validateOrder")
    public ResponseEntity<OrderValidationResult> validateOrder(@RequestBody Order order) {
        return ResponseEntity.ok(validationService.validateOrder(order));
    }

    // Implementing the calcDeliveryPath endpoint
    

}
