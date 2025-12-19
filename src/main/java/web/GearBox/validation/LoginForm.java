package web.GearBox.validation;

import jakarta.validation.constraints.NotBlank; // Add this import
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginForm {

    @NotBlank(message = "{login.username.notnull}")
    private String username;

    @NotBlank(message = "{login.password.notnull}")
    private String password;
}