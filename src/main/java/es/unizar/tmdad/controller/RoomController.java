package es.unizar.tmdad.controller;

import es.unizar.tmdad.controller.exception.UserNotInTheRoomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RoomController {

    @Operation(description = "Connect to the SSE event stream for a specific room",
            parameters = {
                    @Parameter(name = "X-Auth-Firebase", required = true, in = ParameterIn.HEADER, description = "Authentication token (JWT)"),
                    @Parameter(name = "X-Auth-User", required = true, in = ParameterIn.HEADER, description = "Email of the user the X-Auth-Firebase token belongs to.")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connection is established, will start receiving messages in upto a few seconds."),
            @ApiResponse(responseCode = "401", description = "Unauthorised to access this endpoint, are you a member of this room?")
    })
    SseEmitter sendNewTextMessage(@Parameter(description = "Room identifier") Long roomId,
                                  @Parameter(description = "Email of the user who wants to connect to this room") String username) throws UserNotInTheRoomException;
}
