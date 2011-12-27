package us.mn.state.dot.secure
public interface AdDirectory {

  public ArrayList getUserRoles(String userName, boolean shortNames)

  boolean authenticate(String userName, String password)

  String findUser(String userName)
}