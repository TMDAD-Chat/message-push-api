package es.unizar.tmdad.controller.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnauthorizedException extends Exception{

    private final String message;

}
