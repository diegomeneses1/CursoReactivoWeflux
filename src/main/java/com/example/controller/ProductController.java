package com.example.controller;

import com.example.model.Product;
import com.example.service.ProductService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/productos")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Flux<Product> getAllProductos() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Product> getProductoById(@PathVariable String id) {
        return productService.findById(id);
    }

    @PostMapping
    public Mono<Product> createProducto(@RequestBody Product producto) {
        return productService.save(producto);
    }

    @PutMapping("/{id}")
    public Mono<Product> updateProducto(@PathVariable String id, @RequestBody Product product) {
        return productService.findById(id)
                .flatMap(existingProducto -> {
                    existingProducto.setNombre(product.getNombre());
                    existingProducto.setPrecio(product.getPrecio());
                    return productService.save(existingProducto);
                });
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteProducto(@PathVariable String id) {
        return productService.deleteById(id);
    }
}
