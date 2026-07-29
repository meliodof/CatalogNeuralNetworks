package project.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank(message = "Текущий пароль обязателен")
    private String currentPassword;

    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
    private String newPassword;

    @NotBlank(message = "Подтвердите пароль")
    private String confirmPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
