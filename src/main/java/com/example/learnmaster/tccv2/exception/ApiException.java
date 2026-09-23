package com.example.learnmaster.tccv2.exception;

import org.springframework.http.HttpStatus;

// Erro de regra de negocio: vira { "mensagem": ... } com o status indicado
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException naoEncontrado() {
        return new ApiException(HttpStatus.NOT_FOUND, "Item não encontrado.");
    }
}
