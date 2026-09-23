package com.example.learnmaster.tccv2.dto;

import jakarta.validation.constraints.*;

public record CadastroRequest(
        @NotBlank(message = "Informe seu nome.")
        @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "Informe seu e-mail.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 255, message = "O e-mail pode ter no máximo 255 caracteres.")
        String email,

        @NotNull(message = "Informe uma senha.")
        String senha) {
}
