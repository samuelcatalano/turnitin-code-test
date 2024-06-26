package integrations.turnitin.com.membersearcher.model;

public class User {

	private String id;
	private String name;
	private String email;

	public String getId() {
		return id;
	}

	public User setId(final String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public User setName(final String name) {
		this.name = name;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public User setEmail(final String email) {
		this.email = email;
		return this;
	}
}
