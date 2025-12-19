package web.GearBox.service;

import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import web.GearBox.model.Product;
import web.GearBox.repository.ProductRepo;

@Service
public class ProductService {

    private ProductRepo repo;

    public ProductService(ProductRepo repo) {
        this.repo = repo;
    }

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public Product getProductById(int id) {
        return repo.findById(id).orElse(null);
    }

    public Product addProduct(Product product, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageName(imageFile.getOriginalFilename());
            product.setImageType(imageFile.getContentType());
            product.setImageData(imageFile.getBytes());
        }
        return repo.save(product);
    }

    public Product updateProduct(int id, Product product, MultipartFile imageFile) throws IOException {
        Product existing = repo.findById(id).orElse(null);
        if (existing != null) {
            product.setId(id);
            if (imageFile != null && !imageFile.isEmpty()) {
                product.setImageName(imageFile.getOriginalFilename());
                product.setImageType(imageFile.getContentType());
                product.setImageData(imageFile.getBytes());
            } else {
                product.setImageName(existing.getImageName());
                product.setImageType(existing.getImageType());
                product.setImageData(existing.getImageData());
            }
            return repo.save(product);
        }
        return null;
    }

    public void deleteProductById(int id) {
        repo.deleteById(id);
    }

    public List<Product> searchProducts(String keyword) {
        return repo.searchProducts(keyword);
    }
}