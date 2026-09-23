package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.*;

public record AlterarEmailRequest(
        @NotBlank(message = "Informe o novo e-mail.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 255, message = "O e-mail pode ter no máximo 255 caracteres.")
        String email,

        @NotBlank(message = "Informe sua senha atual.")
        String senhaAtual) {
}
