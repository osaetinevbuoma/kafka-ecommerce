package com.kafkaecommerce.marketplace.controllers;

import com.kafkaecommerce.marketplace.exceptions.ProductNotFoundException;
import com.kafkaecommerce.marketplace.records.CartProduct;
import com.kafkaecommerce.marketplace.records.Product;
import com.kafkaecommerce.marketplace.services.MarketplaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/marketplace")
public class MarketplaceController {
    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @GetMapping("/products")
    public List<Product> listProducts() {
        return this.marketplaceService.listProducts();
    }

    @PostMapping("/add")
    public ResponseEntity<CartProduct> addToCart(@RequestBody CartProduct product) {
        try {
            CartProduct cartProduct = this.marketplaceService.addToCart(product);
            return ResponseEntity.ok(cartProduct);
        } catch (ProductNotFoundException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/order")
    public ResponseEntity<Void> placeOrder(@RequestBody Map<String, Integer> user) {
        this.marketplaceService.placeOrder(user.get("userId"));
        return ResponseEntity.ok().build();
    }
}
