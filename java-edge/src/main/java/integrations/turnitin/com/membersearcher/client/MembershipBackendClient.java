package integrations.turnitin.com.membersearcher.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import integrations.turnitin.com.membersearcher.exception.ClientRequestException;
import integrations.turnitin.com.membersearcher.model.MembershipList;
import integrations.turnitin.com.membersearcher.model.User;
import integrations.turnitin.com.membersearcher.model.UserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
public class MembershipBackendClient {

  @Value("${backend.host:http://localhost:8041}")
  private String backendHost;

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  /**
   * Constructs a new instance of the MembershipBackendClient.
   * @param objectMapper The object mapper used for serializing and deserializing JSON data.
   */
  @Autowired
  public MembershipBackendClient(final ObjectMapper objectMapper) {
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    this.objectMapper = objectMapper;
  }

  /**
   * Fetches a list of memberships from the backend API.
   * @return A CompletableFuture that resolves to a MembershipList object containing the fetched memberships.
   */
  public CompletableFuture<MembershipList> fetchMemberships() {
    return makeRequest("GET", backendHost + "/api.php/members", null, MembershipList.class);
  }

  /**
   * Fetches a user from the backend API by their user ID.
   *
   * @param userId The ID of the user to fetch.
   * @return A CompletableFuture that resolves to a User object representing the fetched user.
   */
  public CompletableFuture<User> fetchUser(final String userId) {
    return makeRequest("GET", backendHost + "/api.php/users/" + userId, null, User.class);
  }

  /**
   * Fetches a list of users from the backend API.
   *
   * @return A CompletableFuture that resolves to a UserList object containing the fetched users.
   */
  public CompletableFuture<UserList> fetchUsers() {
    return makeRequest("GET", backendHost + "/api.php/users", null, UserList.class);
  }

  /**
   * Makes an HTTP request to the backend API.
   *
   * @param method       The HTTP method to use for the request (e.g., GET, POST, PUT, DELETE).
   * @param url          The URL of the backend API endpoint.
   * @param body         The request body, if applicable.
   * @param responseType The expected type of the response object.
   * @param <T>          The type parameter representing the expected type of the response object.
   * @return A CompletableFuture that resolves to the response object of the specified type.
   */
  private <T> CompletableFuture<T> makeRequest(final String method, final String url, final Object body, final Class<T> responseType) {
    final URI uri = URI.create(url);

    final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri);
    if (responseType != Void.class) {
      requestBuilder.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }
    String bodyString = "";
    if (body != null) {
      requestBuilder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      try {
        bodyString = objectMapper.writeValueAsString(body);
      } catch (final IOException exception) {
        throw new ClientRequestException("Failed to serialize request body", exception);
      }
    }
    requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(bodyString));

    return makeAsyncHttpRequest(requestBuilder.build(), responseType);
  }

  /**
   * Makes an asynchronous HTTP request to the backend API.
   *
   * @param request      The HTTP request object.
   * @param responseType The expected type of the response object.
   * @param <T>          The type parameter representing the expected type of the response object.
   * @return A CompletableFuture that resolves to the response object of the specified type.
   */
  protected <T> CompletableFuture<T> makeAsyncHttpRequest(final HttpRequest request, final Class<T> responseType) {
    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .exceptionally(ex -> {
          throw new ClientRequestException("Failed to call URL: " + request.uri().toString(), ex);
        })
        .thenApply(response -> {
          final HttpStatus status = HttpStatus.valueOf(response.statusCode());

          if (status.is2xxSuccessful()) {
            try {
              return responseType == Void.class ? null : objectMapper.readValue(response.body(), responseType);
            } catch (final IOException ex) {
              throw new ClientRequestException("Could not deserialize the response", ex);
            }
          }
          throw new ClientRequestException("Bad Request");
        });
  }
}
