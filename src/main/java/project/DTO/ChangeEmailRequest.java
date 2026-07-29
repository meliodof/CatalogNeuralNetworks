package project.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ChangeEmailRequest {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String newEmail;

    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
}
