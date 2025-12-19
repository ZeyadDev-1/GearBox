package web.GearBox.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import web.GearBox.model.CartItem;
import web.GearBox.model.Product;
import web.GearBox.service.ProductService;
import web.GearBox.validation.LoginForm;

@Controller
@RequestMapping("/")
public class ProductController {
    private ProductService service;
    private MessageSource messageSource;

    public ProductController(ProductService service, MessageSource messageSource) {
        this.service = service;
        this.messageSource = messageSource;
    }

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    
 // Define category keys
    private static final String[] CATEGORY_KEYS = {
        "category.laptop", "category.headphone", "category.mobile",
        "category.electronics", "category.toys", "category.fashion"
    };
    
 // Method to get translated categories
    private List<String> getTranslatedCategories() {
        List<String> translatedCategories = new ArrayList<>();
        for (String key : CATEGORY_KEYS) {
            String translatedCategory = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
            translatedCategories.add(translatedCategory);
        }
        return translatedCategories;
    }

    @GetMapping("")
    public String getHomePage(Model model, @RequestParam(required = false) String category) {
        List<Product> products = service.getAllProducts();
        if (category != null && !category.isEmpty()) {
            // Use translated category for filtering.
            String translatedCategory = category;
            // Map the input category back to the English key for filtering
            for (String key : CATEGORY_KEYS) {
                String transCategory = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
                if (transCategory.equals(category)) {
                    // Map to English category name for filtering
                    translatedCategory = key.replace("category.", "");
                    break;
                }
            }
            String finalCategory = translatedCategory;
            products = products.stream()
                .filter(p -> p.getCategories() != null && p.getCategories().contains(finalCategory))
                .collect(Collectors.toList());
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", getTranslatedCategories());
        return "home";
    }

    @GetMapping("/about_us")
    public String showAboutUs(Model model) {
        model.addAttribute("categories", getTranslatedCategories());
        return "about_us";
    }
    
    @GetMapping("/contact_us")
    public String showContactUs(Model model) {
        model.addAttribute("categories", getTranslatedCategories());
        return "contact_us";
    }

    @GetMapping("/product/{prodid}")
    public String getProductById(@PathVariable("prodid") int prodid, Model model, HttpSession session) {
        Product product = service.getProductById(prodid);
        if (product != null) {
            model.addAttribute("product", product);
            model.addAttribute("isAdmin", isAdminLoggedIn(session));
            return "product";
        }
        return "redirect:/";
    }

    @GetMapping("/add_product")
    public String showAddProductForm(Model model, HttpSession session) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("product", new Product());
        model.addAttribute("categories", getTranslatedCategories());
        return "add_product";
    }

    @PostMapping("/add_product")
    public String addProduct(@Valid @ModelAttribute Product product, BindingResult result,
                             @RequestPart MultipartFile imageFile, Model model, HttpSession session) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }
        // Custom validation for imageFile
        if (imageFile == null || imageFile.isEmpty()) {
            result.rejectValue("imageName", "product.image.notnull", "Image is required");
        }
        if (product.getCategories() == null || product.getCategories().isEmpty()) {
            result.rejectValue("categories", "product.categories.notnull", "At least one category is required");
        }
        if (result.hasErrors()) {
            model.addAttribute("categories", getTranslatedCategories());
            return "add_product";
        }
        try {
            // Map translated categories back to English keys
            List<String> englishCategories = product.getCategories().stream()
                .map(transCategory -> {
                    for (String key : CATEGORY_KEYS) {
                        String trans = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
                        if (trans.equals(transCategory)) {
                            return key.replace("category.", "");
                        }
                    }
                    return transCategory; // Fallback
                })
                .collect(Collectors.toList());
            product.setCategories(englishCategories);
            service.addProduct(product, imageFile);
            model.addAttribute("message", messageSource.getMessage("product.add.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/admin";
        } catch (Exception e) {
            model.addAttribute("error", messageSource.getMessage("product.add.error", null, LocaleContextHolder.getLocale()));
            model.addAttribute("categories", getTranslatedCategories());
            return "add_product";
        }
    }

    @GetMapping("/product/{productId}/image")
    @ResponseBody
    public byte[] getImageByProductId(@PathVariable("productId") int productId) {
        Product product = service.getProductById(productId);
        return product != null ? product.getImageData() : null;
    }

    @GetMapping("/product/update/{id}")
    public String showUpdateForm(@PathVariable("id") int id, Model model, HttpSession session) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }
        Product product = service.getProductById(id);
        if (product != null) {
            // Translate stored categories to current locale
            List<String> translatedCategories = product.getCategories().stream()
                .map(category -> messageSource.getMessage("category." + category, null, category, LocaleContextHolder.getLocale()))
                .collect(Collectors.toList());
            product.setCategories(translatedCategories);
            model.addAttribute("product", product);
            model.addAttribute("categories", getTranslatedCategories());
            return "update_product";
        }
        return "redirect:/admin";
    }

    @PostMapping("/product/update/{id}")
    public String updateProduct(@PathVariable("id") int id, @Valid @ModelAttribute Product product, BindingResult result,
                                @RequestPart MultipartFile imageFile, Model model, HttpSession session) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }
        // Custom validation for imageFile
        if (imageFile == null || imageFile.isEmpty()) {
            result.rejectValue("imageName", "product.image.notnull", "Image is required");
        }
        if (product.getCategories() == null || product.getCategories().isEmpty()) {
            result.rejectValue("categories", "product.categories.notnull", "At least one category is required");
        }
        if (result.hasErrors()) {
            model.addAttribute("categories", getTranslatedCategories());
            return "update_product";
        }
        try {
            // Map translated categories back to English keys
            List<String> englishCategories = product.getCategories().stream()
                .map(transCategory -> {
                    for (String key : CATEGORY_KEYS) {
                        String trans = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
                        if (trans.equals(transCategory)) {
                            return key.replace("category.", "");
                        }
                    }
                    return transCategory; // Fallback
                })
                .collect(Collectors.toList());
            product.setCategories(englishCategories);
            Product updated = service.updateProduct(id, product, imageFile);
            if (updated != null) {
                model.addAttribute("message", messageSource.getMessage("product.update.success", null, LocaleContextHolder.getLocale()));
                return "redirect:/admin";
            }
            model.addAttribute("error", messageSource.getMessage("product.update.notfound", null, LocaleContextHolder.getLocale()));
            model.addAttribute("categories", getTranslatedCategories());
            return "update_product";
        } catch (IOException e) {
            model.addAttribute("error", messageSource.getMessage("product.update.error", null, LocaleContextHolder.getLocale()));
            model.addAttribute("categories", getTranslatedCategories());
            return "update_product";
        }
    }

    @PostMapping("/product/delete/{id}")
    public String deleteProductById(@PathVariable("id") int id, Model model, HttpSession session) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }
        Product product = service.getProductById(id);
        if (product != null) {
            service.deleteProductById(id);
            model.addAttribute("message", messageSource.getMessage("product.delete.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/admin";
        }
        model.addAttribute("error", messageSource.getMessage("product.delete.notfound", null, LocaleContextHolder.getLocale()));
        return "redirect:/admin";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam(required = false) String keyword, Model model) {
        List<Product> products = keyword != null && !keyword.isEmpty() 
            ? service.searchProducts(keyword) 
            : service.getAllProducts();
        model.addAttribute("searchResults", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", getTranslatedCategories());
        return "navbar";
    }

    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {
        String cartAttribute = isAdminLoggedIn(session) ? "adminCart" : "cart";
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute(cartAttribute);
        if (cartItems == null) {
            cartItems = new ArrayList<>();
            session.setAttribute(cartAttribute, cartItems);
        }
        cartItems = cartItems.stream()
            .filter(item -> {
                if (item == null || item.getProduct() == null) {
                    model.addAttribute("error", messageSource.getMessage("cart.product.invalid", null, LocaleContextHolder.getLocale()));
                    return false;
                }
                Product dbProduct = service.getProductById(item.getProduct().getId());
                if (dbProduct == null) {
                    model.addAttribute("error", messageSource.getMessage("cart.product.notfound", null, LocaleContextHolder.getLocale()));
                    return false;
                }
                item.setProduct(dbProduct);
                if (item.getQuantity() > dbProduct.getStockQuantity()) {
                    item.setQuantity(dbProduct.getStockQuantity());
                }
                return item.getQuantity() > 0;
            })
            .collect(Collectors.toList());
        BigDecimal totalPrice = cartItems.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        session.setAttribute(cartAttribute, cartItems);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("categories", getTranslatedCategories());
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") int productId, HttpSession session, Model model) {
        String cartAttribute = isAdminLoggedIn(session) ? "adminCart" : "cart";
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute(cartAttribute);
        if (cartItems == null) {
            cartItems = new ArrayList<>();
            session.setAttribute(cartAttribute, cartItems);
        }
        Product product = service.getProductById(productId);
        if (product != null && product.getStockQuantity() > 0) {
            CartItem existingItem = cartItems.stream()
                .filter(item -> item.getProductId() == productId)
                .findFirst()
                .orElse(null);
            if (existingItem != null) {
                int newQuantity = existingItem.getQuantity() + 1;
                if (newQuantity <= product.getStockQuantity()) {
                    existingItem.setQuantity(newQuantity);
                } else {
                    existingItem.setQuantity(product.getStockQuantity());
                }
            } else {
                cartItems.add(new CartItem(product, Math.min(1, product.getStockQuantity())));
            }
            session.setAttribute(cartAttribute, cartItems);
        } else {
            model.addAttribute("error", messageSource.getMessage("cart.product.notfound", null, LocaleContextHolder.getLocale()));
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam("itemId") int itemId, 
                             @RequestParam("action") String action, 
                             HttpSession session) {
        String cartAttribute = isAdminLoggedIn(session) ? "adminCart" : "cart";
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute(cartAttribute);
        if (cartItems != null) {
            CartItem cartItem = cartItems.stream()
                .filter(item -> item.getProductId() == itemId)
                .findFirst()
                .orElse(null);
            if (cartItem != null) {
                Product dbProduct = service.getProductById(itemId);
                if (dbProduct != null) {
                    int currentQty = cartItem.getQuantity();
                    if ("increase".equals(action) && currentQty < dbProduct.getStockQuantity()) {
                        cartItem.setQuantity(currentQty + 1);
                    } else if ("increase".equals(action)) {
                        cartItem.setQuantity(dbProduct.getStockQuantity());
                    } else if ("decrease".equals(action) && currentQty > 1) {
                        cartItem.setQuantity(currentQty - 1);
                    }
                    cartItem.setProduct(dbProduct);
                    session.setAttribute(cartAttribute, cartItems);
                }
            }
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("itemId") int itemId, HttpSession session) {
        String cartAttribute = isAdminLoggedIn(session) ? "adminCart" : "cart";
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute(cartAttribute);
        if (cartItems != null) {
            cartItems.removeIf(item -> item.getProductId() == itemId);
            session.setAttribute(cartAttribute, cartItems);
        }
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model) {
        String cartAttribute = isAdminLoggedIn(session) ? "adminCart" : "cart";
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute(cartAttribute);
        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        cartItems = cartItems.stream()
            .filter(item -> {
                if (item == null || item.getProduct() == null) return false;
                Product dbProduct = service.getProductById(item.getProduct().getId());
                if (dbProduct == null) return false;
                item.setProduct(dbProduct);
                if (item.getQuantity() > dbProduct.getStockQuantity()) {
                    item.setQuantity(dbProduct.getStockQuantity());
                }
                return item.getQuantity() > 0;
            })
            .collect(Collectors.toList());
        BigDecimal totalPrice = cartItems.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        session.setAttribute(cartAttribute, cartItems);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("categories", getTranslatedCategories());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(HttpSession session, Model model) {
        String cartAttribute = isAdminLoggedIn(session) ? "adminCart" : "cart";
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute(cartAttribute);
        if (cartItems != null && !cartItems.isEmpty()) {
            try {
                for (CartItem cartItem : cartItems) {
                    Product dbProduct = service.getProductById(cartItem.getProductId());
                    if (dbProduct != null) {
                        int newStock = dbProduct.getStockQuantity() - cartItem.getQuantity();
                        if (newStock >= 0) {
                            dbProduct.setStockQuantity(newStock);
                                          
                                           
                            service.updateProduct(dbProduct.getId(), dbProduct, null);
                        } else {
                            model.addAttribute("error", messageSource.getMessage("cart.stock.insufficient", 
                                new Object[]{dbProduct.getName()}, LocaleContextHolder.getLocale()));
                            return "redirect:/checkout";
                        }
                    }
                }
                session.removeAttribute(cartAttribute);
                model.addAttribute("message", messageSource.getMessage("checkout.success", null, LocaleContextHolder.getLocale()));
                return "redirect:/";
            } catch (IOException e) {
                model.addAttribute("error", messageSource.getMessage("checkout.error", null, LocaleContextHolder.getLocale()));
                return "redirect:/checkout";
            }
        }
        return "redirect:/cart";
    }

    @GetMapping("/clear-cart")
    public String clearCart(HttpSession session) {
        String cartAttribute = isAdminLoggedIn(session) ? "adminCart" : "cart";
        session.removeAttribute(cartAttribute);
        return "redirect:/cart";
    }

    @GetMapping("/admin/login")
    public String showAdminLogin(Model model, HttpSession session) {
        if (isAdminLoggedIn(session)) {
            return "redirect:/admin";
        }
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("error", session.getAttribute("loginError"));
        session.removeAttribute("loginError");
        return "admin_login";
    }

    @PostMapping("/admin/login")
    public String processAdminLogin(@Valid @ModelAttribute("loginForm") LoginForm loginForm, BindingResult result,
                                    HttpSession session, Model model) {
        if (result.hasErrors()) {
            return "admin_login";
        }
        if (ADMIN_USERNAME.equals(loginForm.getUsername()) && ADMIN_PASSWORD.equals(loginForm.getPassword())) {
            session.setAttribute("adminLoggedIn", true);
            return "redirect:/admin";
        } else {
            model.addAttribute("loginError", messageSource.getMessage("login.error", null, LocaleContextHolder.getLocale()));
            return "admin_login";
        }
    }

    @GetMapping("/admin")
    public String showAdminDashboard(Model model, HttpSession session) {
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }
        List<Product> products = service.getAllProducts();
        model.addAttribute("products", products);
        return "admin";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.removeAttribute("adminLoggedIn");
        return "redirect:/";
    }

    private boolean isAdminLoggedIn(HttpSession session) {
        return session.getAttribute("adminLoggedIn") != null && (boolean) session.getAttribute("adminLoggedIn");
    }
}