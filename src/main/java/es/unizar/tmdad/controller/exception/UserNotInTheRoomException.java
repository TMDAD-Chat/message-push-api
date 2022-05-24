package es.unizar.tmdad.controller.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserNotInTheRoomException extends Exception{

    private final long roomId;
    private final String userId;
}
