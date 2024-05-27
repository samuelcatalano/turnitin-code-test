package integrations.turnitin.com.membersearcher.service;

import integrations.turnitin.com.membersearcher.client.MembershipBackendClient;
import integrations.turnitin.com.membersearcher.model.MembershipList;
import integrations.turnitin.com.membersearcher.model.User;
import integrations.turnitin.com.membersearcher.model.UserList;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class MembershipService {

  private static final Logger log = Logger.getLogger(MembershipService.class.getSimpleName());

  private final MembershipBackendClient membershipBackendClient;

  /**
   * Constructor.
   * @param membershipBackendClient the membershipBackendClient
   */
  public MembershipService(final MembershipBackendClient membershipBackendClient) {
    this.membershipBackendClient = membershipBackendClient;
  }

  /**
   * Method to fetch all memberships with their associated user details included.
   * This method calls out to the php-backend service and fetches all memberships,
   * it then calls to fetch all users in one request and associates them with their
   * corresponding membership.
   *
   * @return A CompletableFuture containing a fully populated MembershipList object.
   */
  public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {
    final var membershipsFuture = membershipBackendClient.fetchMemberships();
    final var usersFuture = membershipBackendClient.fetchUsers();

    return membershipsFuture.thenCombine(usersFuture, (memberships, users) -> {
      final Map<String, User> userMap = users.getUsers().stream()
           .collect(Collectors.toMap(User::getId, user -> user));

      memberships.getMemberships().forEach(membership -> {
        final User user = userMap.get(membership.getUserId());
        if (user != null) {
          membership.setUser(user);
        }
      });

      return memberships;
    }).exceptionally(ex -> {
      // Log the exception
      log.warning("Error fetching memberships or users: " + ex.getMessage());
      // Handle the error scenario
      return new MembershipList().setMemberships(Collections.emptyList()); // Returning an empty MembershipList at this moment
    });
  }
}
