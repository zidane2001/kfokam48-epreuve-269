package com.kfokam48.presence55.dto;

/** Format d'erreur impose : { code, message } — jamais de stack trace (B4). */
public record ApiError(String code, String message) { }
