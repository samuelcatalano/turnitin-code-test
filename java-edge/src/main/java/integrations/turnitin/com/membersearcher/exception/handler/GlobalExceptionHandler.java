package integrations.turnitin.com.membersearcher.exception.handler;

import integrations.turnitin.com.membersearcher.exception.ClientRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles exceptions of type `ClientRequestException` that may occur in the application's controllers.
   * <p>
   * @param e        the `TrackUserException` instance that was thrown
   * @param request  the `WebRequest` object containing information about the current request
   * @return a `ResponseEntity` containing an `ErrorResponse` object with the appropriate HTTP status and error details
   */
  @ExceptionHandler(value = ClientRequestException.class)
  public ResponseEntity<Object> handleTrackUserException(final ClientRequestException e, final WebRequest request) {
    final Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.NOT_FOUND.value());
    body.put("error", "Not Found");
    body.put("message", e.getMessage());
    body.put("path", request.getDescription(false));

    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }
}
