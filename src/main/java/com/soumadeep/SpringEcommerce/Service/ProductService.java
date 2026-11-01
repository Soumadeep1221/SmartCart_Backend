package com.soumadeep.SpringEcommerce.Service;

import com.soumadeep.SpringEcommerce.Entity.Product;
import com.soumadeep.SpringEcommerce.Repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo productRepo;
    private final ChatClient chatClient;
    private final PgVectorStore vectorStore;
    private final AIImageGenService aiImageGenService;

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Product getProductById(int id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with id: " + id));
    }

    public Product addProduct(Product product, MultipartFile image) throws IOException {
        product.setImageName(image.getOriginalFilename());
        product.setImageType(image.getContentType());
        product.setImageData(image.getBytes());

        Product saveProduct=productRepo.save(product);

        String content=String.format("""
                Product Name: %s
                Description: %s
                Brand: %s
                Category: %s
                Price: %.2f
                Release Date: %s
                Available: %s
                Stock: %s
                """,
                saveProduct.getName(),
                saveProduct.getDescription(),
                saveProduct.getBrand(),
                saveProduct.getCategory(),
                saveProduct.getPrice(),
                saveProduct.getReleaseDate(),
                saveProduct.isProductAvailable(),
                saveProduct.getStockQuantity()
        );

        Document document=new Document(
                UUID.randomUUID().toString(),
                content,
                Map.of("type", "PRODUCT", "productId", saveProduct.getId()));

        vectorStore.add(List.of(document));

        return saveProduct;
    }

    public Product updateProduct(int id, Product product, MultipartFile image) throws IOException {
        Product existing = getProductById(id);   // 404 if missing

        if (image != null && !image.isEmpty()) {
            product.setImageName(image.getOriginalFilename());
            product.setImageType(image.getContentType());
            product.setImageData(image.getBytes());
        } else {
            product.setImageName(existing.getImageName());
            product.setImageType(existing.getImageType());
            product.setImageData(existing.getImageData());
        }
        Product saveProduct=productRepo.save(product);

        Filter.Expression filter = new FilterExpressionBuilder()
                .eq("productId", saveProduct.getId())
                .build();

        vectorStore.delete(filter);

        String content=String.format("""
                Product Name: %s
                Description: %s
                Brand: %s
                Category: %s
                Price: %.2f
                Release Date: %s
                Available: %s
                Stock: %s
                """,
                saveProduct.getName(),
                saveProduct.getDescription(),
                saveProduct.getBrand(),
                saveProduct.getCategory(),
                saveProduct.getPrice(),
                saveProduct.getReleaseDate(),
                saveProduct.isProductAvailable(),
                saveProduct.getStockQuantity()
        );

        Document document=new Document(
                UUID.randomUUID().toString(),
                content,
                Map.of("type", "PRODUCT", "productId", saveProduct.getId()));

        vectorStore.add(List.of(document));

        return saveProduct;
    }

    public void deleteProduct(int id) {
        getProductById(id);
        productRepo.deleteById(id);

        Filter.Expression filter = new FilterExpressionBuilder()
                .eq("productId", id)
                .build();

        vectorStore.delete(filter);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        return productRepo.searchProducts(keyword);
    }

    public String generateDescription(String name, String category) {

        String prompt=String.format("""
                Write a concise and professional product description for an e-commerce listing.
                
                Product Name: %s
                Category: %s
                
                Keep it simple, engaging, and highlight its primary features or benefits.
                Avoid technical jargon and keep it customer-friendly.
                Limit the description to 200 characters maximum.
                """,name,category);

        String desc=chatClient.prompt(prompt).
                call().
                chatResponse().
                getResult().
                getOutput().
                getText();

        return desc;
    }

    public byte[] generateImage(String name, String category, String description) {

        String prompt=String.format("""
                 Generate a highly realistic, professional-grade e-commerce product image.
                
                 Product Details:
                 - Category: %s
                 - Name: '%s'
                 - Description: %s
                
                 Requirements:
                 - Use a clean, minimalistic, white or very light grey background.
                 - Ensure the product is well-lit with soft, natural-looking lighting.
                 - Add realistic shadows and soft reflections to ground the product naturally.
                 - No humans, brand logos, watermarks, or text overlays should be visible.
                 - Showcase the product from its most flattering angle that highlights key features.
                 - Ensure the product occupies a prominent position in the frame, centered or slightly off-centered.
                 - Maintain a high resolution and sharpness, ensuring all textures, colors, and details are clear.
                 - Follow the typical visual style of top e-commerce websites like Amazon, Flipkart, or Shopify.
                 - Make the product appear life-like and professionally photographed in a studio setup.
                 - The final image should look immediately ready for use on an e-commerce website without further editing.
                """,category,name,description);

        return  aiImageGenService.generateImage(prompt);
    }
}