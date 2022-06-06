package es.unizar.tmdad.controller;

import es.unizar.tmdad.controller.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface UserController {
    @GetMapping(value = "/{email}", headers = {"X-Auth-Firebase"})
    @Operation(description = "Connect to the SSE event stream for a specific user (P2P messaging)",
            parameters = {
                    @Parameter(name = "X-Auth-Firebase", required = true, in = ParameterIn.HEADER, description = "Authentication token (JWT)"),
                    @Parameter(name = "X-Auth-User", required = true, in = ParameterIn.HEADER, description = "Email of the user the X-Auth-Firebase token belongs to.")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connection is established, will start receiving messages in upto a few seconds."),
            @ApiResponse(responseCode = "401", description = "Unauthorised to access this endpoint, are you the right user?")
    })
    SseEmitter getMessagesOfUser(@Parameter(description = "Other user email") @PathVariable("email") String email,
                                 @Parameter(description = "Email of user who wants to connect") @RequestHeader("X-Auth-User") String authEmail) throws UnauthorizedException;
}
