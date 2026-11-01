package com.soumadeep.SpringEcommerce.Controller;

import com.soumadeep.SpringEcommerce.Entity.Product;
import com.soumadeep.SpringEcommerce.Enum.TokenAction;
import com.soumadeep.SpringEcommerce.Service.ProductService;
import com.soumadeep.SpringEcommerce.Service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final TokenService tokenService;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts()
    {
        return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable int id)
    {
        return new ResponseEntity<>(productService.getProductById(id),HttpStatus.OK);
    }

    @GetMapping("product/{productId}/image")
    public ResponseEntity<byte[]> getImgByProductId(@PathVariable int productId)
    {
        return new ResponseEntity<>(productService.getProductById(productId).getImageData(),HttpStatus.OK);
    }

    @PostMapping("/product/generate-description")
    public ResponseEntity<String> generateDescription(@RequestParam String name, @RequestParam String category, Authentication authentication) {
        String email = authentication.getName();
        String aiDesc = tokenService.charge(email, TokenAction.DESCRIPTION, () -> productService.generateDescription(name, category));
        return ResponseEntity.ok()
                .header("X-Token-Balance", String.valueOf(tokenService.getBalance(email)))
                .body(aiDesc);
    }

    @PostMapping("/product/generate-image")
    public ResponseEntity<byte[]> generateImage(@RequestParam String name, @RequestParam String category, @RequestParam String description, Authentication authentication) {
        String email = authentication.getName();
        byte[] aiImage = tokenService.charge(email, TokenAction.IMAGE, () -> productService.generateImage(name, category, description));
        return ResponseEntity.ok()
                .header("X-Token-Balance", String.valueOf(tokenService.getBalance(email)))
                .body(aiImage);
    }

    @PostMapping("/product")
    public ResponseEntity<?> addProduct(@RequestPart Product product, @RequestPart MultipartFile imageFile)
    {
        try {
            Product product1=productService.addProduct(product,imageFile);
            return new ResponseEntity<>(product1,HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable int id, @RequestPart Product product, @RequestPart(required = false) MultipartFile imageFile){
        try {
            Product product1=productService.updateProduct(id,product,imageFile);
            return new ResponseEntity<>(product1,HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id)
    {
        try {
            productService.deleteProduct(id);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("This product cannot be deleted because it is part of existing orders.", HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword)
    {
        List<Product> products=productService.searchProducts(keyword);
        return new ResponseEntity<>(products,HttpStatus.OK);
    }
}
