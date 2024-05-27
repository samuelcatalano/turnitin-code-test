package integrations.turnitin.com.membersearcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;
import integrations.turnitin.com.membersearcher.client.MembershipBackendClient;
import integrations.turnitin.com.membersearcher.model.Membership;
import integrations.turnitin.com.membersearcher.model.MembershipList;
import integrations.turnitin.com.membersearcher.model.User;
import integrations.turnitin.com.membersearcher.model.UserList;
import integrations.turnitin.com.membersearcher.service.MembershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // In this mode, Mockito does not check whether a mock's methods are called with the correct arguments
class MembershipServiceTest {

	@InjectMocks
	private MembershipService membershipService;
	@Mock
	private MembershipBackendClient membershipBackendClient;

  private MembershipList members;
  private User userOne;
	private User userTwo;

	/**
	 * Initializes test objects and mocks.
	 */
	@BeforeEach
	public void init() {
    members = new MembershipList()
        .setMemberships(List.of(
            new Membership()
                .setId("a")
                .setRole("instructor")
                .setUserId("1"),
            new Membership()
                .setId("b")
                .setRole("student")
                .setUserId("2")
        ));
		userOne = new User()
				.setId("1")
				.setName("test one")
				.setEmail("test1@example.com");
		userTwo = new User()
				.setId("2")
				.setName("test two")
				.setEmail("test2@example.com");

		// Create a CompletableFuture with users
		final CompletableFuture<UserList> usersFuture = new CompletableFuture<>();
		usersFuture.complete(new UserList().setUsers(List.of(userOne, userTwo)));

		// Configure mocks
		when(membershipBackendClient.fetchMemberships()).thenReturn(CompletableFuture.completedFuture(members));
		when(membershipBackendClient.fetchUsers()).thenReturn(usersFuture);

		when(membershipBackendClient.fetchMemberships()).thenReturn(CompletableFuture.completedFuture(members));
		when(membershipBackendClient.fetchUser("1")).thenReturn(CompletableFuture.completedFuture(userOne));
		when(membershipBackendClient.fetchUser("2")).thenReturn(CompletableFuture.completedFuture(userTwo));
	}

	/**
	 * Test method for {@link MembershipService#fetchAllMembershipsWithUsers()}.
	 * Verifies that the method returns a {@link MembershipList} with users fetched.
	 *
	 * @throws Exception if an error occurs during the test
	 */
	@Test
	void testFetchAllMemberships() throws Exception {
		final MembershipList members = membershipService.fetchAllMembershipsWithUsers().get();
		assertThat(members.getMemberships().get(0).getUser()).isEqualTo(userOne);
		assertThat(members.getMemberships().get(1).getUser()).isEqualTo(userTwo);
	}

	/**
	 * Test method for {@link MembershipService#fetchAllMembershipsWithUsers()}.
	 * Verifies that the method returns an empty {@link MembershipList} when fetching memberships fails.
	 */
	@Test
	void testFetchMembershipsException() {
		when(membershipBackendClient.fetchMemberships()).thenReturn(
				CompletableFuture.failedFuture(new RuntimeException("Memberships fetch failed"))
		);

		final MembershipList members = membershipService.fetchAllMembershipsWithUsers().join();
		assertThat(members.getMemberships()).isEmpty();
	}

	/**
	 * Test method for {@link MembershipService#fetchAllMembershipsWithUsers()}.
	 * Verifies that the method returns a {@link MembershipList} with null users when fetching users fails.
	 */
	@Test
	void testFetchUsersException() {
		when(membershipBackendClient.fetchUsers()).thenReturn(
				CompletableFuture.failedFuture(new RuntimeException("Users fetch failed"))
		);

		assertThat(members).isNotNull();
		assertThat(members.getMemberships()).isNotNull();
		assertThat(members.getMemberships().get(0).getUser()).isNull();
		assertThat(members.getMemberships().get(1).getUser()).isNull();
	}

	/**
	 * Test method for {@link MembershipService#fetchAllMembershipsWithUsers()}.
	 * Verifies that the method returns a {@link MembershipList} with null membership list is empty.
	 */
	@Test
	void testEmptyUsersList() throws Exception {
		final UserList emptyUsers = new UserList().setUsers(List.of());

		when(membershipBackendClient.fetchUsers()).thenReturn(CompletableFuture.completedFuture(emptyUsers));

		final MembershipList members = membershipService.fetchAllMembershipsWithUsers().get();
		assertThat(members.getMemberships()).isNotEmpty();
		assertThat(members.getMemberships().get(0).getUser()).isNull();
		assertThat(members.getMemberships().get(1).getUser()).isNull();
	}
}
