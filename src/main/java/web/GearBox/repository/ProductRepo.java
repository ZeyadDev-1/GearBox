package web.GearBox.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import web.GearBox.model.Product;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE" +
           " LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR" +
           " LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR" +
           " LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR" +
           " EXISTS (SELECT c FROM p.categories c WHERE LOWER(c) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchProducts(String keyword);
}