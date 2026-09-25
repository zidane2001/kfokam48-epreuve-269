package com.kfokam48.presence55.exception;

/** Exception metier portant le code d'erreur stable du contrat d'API (ex: CODE_EXPIRE). */
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
}
