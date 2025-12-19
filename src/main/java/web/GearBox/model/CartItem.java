package web.GearBox.model;

import java.math.BigDecimal;

public class CartItem {
    private Product product;
    private int quantity;
    private BigDecimal totalPrice; //New field to store the calculated total price

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.calculateTotalPrice();
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        this.calculateTotalPrice();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.calculateTotalPrice();
    }

    public int getProductId() {
        return product != null ? product.getId() : -1;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    private void calculateTotalPrice() {
        if (product != null && product.getPrice() != null) {
            this.totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }

    @Override
    public String toString() {
        return "CartItem{" +
               "product=" + (product != null ? product.getId() : "null") +
               ", quantity=" + quantity +
               ", totalPrice=" + totalPrice +
               '}';
    }
}