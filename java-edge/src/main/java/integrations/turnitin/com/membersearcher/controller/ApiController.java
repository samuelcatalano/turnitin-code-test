package integrations.turnitin.com.membersearcher.controller;

import java.util.concurrent.CompletableFuture;

import integrations.turnitin.com.membersearcher.model.MembershipList;
import integrations.turnitin.com.membersearcher.service.MembershipService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ApiController {

	private final MembershipService membershipService;

	/**
	 * Constructor.
	 * @param membershipService the membership service class
	 */
	public ApiController(final MembershipService membershipService) {
		this.membershipService = membershipService;
	}

	/**
	 * Fetches a list of all memberships, including associated users.
	 * This method fetches all memberships from the membership service and returns them
	 * as a {@link CompletableFuture} containing a {@link MembershipList} object. The
	 * {@link MembershipList} object will contain information about each membership,
	 * including potentially associated user data.
	 *
	 * @return A {@link CompletableFuture} containing a {@link MembershipList} object with information about all memberships.
   */
	@GetMapping("/course/members")
	public CompletableFuture<MembershipList> fetchAllMemberships() {
		return membershipService.fetchAllMembershipsWithUsers();
	}
}
