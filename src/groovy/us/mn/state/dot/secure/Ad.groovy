package us.mn.state.dot.secure

import us.mn.state.dot.secure.LdapDataStore
import org.jsecurity.authc.AuthenticationException
import us.mn.state.dot.mnroad.MultiValuedMap

public class Ad implements AdDirectory {

  def connectionURL="ldap://ldapad:389"
  // CN=LDAPBrowse,OU=LDAP Testing,OU=Enterprise Administration,DC=ad,DC=dot,DC=state,DC=mn,DC=us
  def connectionName="ldapbrowse"
  def connectionPassword="ldapbrowse"
  def contextFactory="com.sun.jndi.ldap.LdapCtxFactory"
  def userBase="DC=ad,DC=dot,DC=state,DC=mn,DC=us"
  def userSearch="(&(sAMAccountName={0})(objectClass=person))"
  def userSubtree="true"
  def userRoleName="memberOf"
  def referrals="follow"
  def roleBase="DC=ad,DC=dot,DC=state,DC=mn,DC=us"
  def roleSearch="(&(member={0})(objectClass=group))"

  LdapDataStore lds = new LdapDataStore()

  // Copy configuration values if Config.groovy supplies them  
  public def configure =  { aMap ->
    lds.setContextFactory(aMap?.ldap.context.factory?:contextFactory)
    lds.setConnectionURL(aMap?.ldap.server.url?:connectionURL)
    lds.setConnectionName(aMap?.ldap.search.user ?:connectionName)
    lds.setConnectionPassword(aMap?.ldap.search.pass ?:connectionPassword)
    lds.setUserBase(aMap?.ldap.search.base?:userBase)
    lds.setUserSearch(aMap?.ldap.usersearch.filter?:userSearch)
    lds.setUserRoleName(aMap?.ldap.userRoleName?:userRoleName)
    lds.setReferrals(aMap?.ldap.referrals?:referrals)
    lds.setRoleBase(aMap?.ldap.role.base?:roleBase)
    lds.setRoleSearch(aMap?.ldap.role.search?:roleSearch)
  }

  public ArrayList getUserRoles(String userName, boolean shortNames) {
    return lds.getUserRoles(userName, shortNames)
  }

  public boolean authenticate(String userName, String password) {
    boolean b = lds.bind(userName, password)
    if (!b) {
      throw new AuthenticationException()
    }
    return b
  }

  public String findUser(String userName) {
    return lds.getDn(userName)
  }

  public MultiValuedMap getUserAttrs(String userName) {
    MultiValuedMap al = lds.getUserAttrs(userName)
    return al
  }

  MultiValuedMap mockRoles() {
    MultiValuedMap mvm = new MultiValuedMap()
    def carr1denRoles = ["MRL Dfs Research Section","MRL USERS","DOTemployees"]
    mvm.put("carr1den",carr1denRoles)
    def burn1tomRoles = ["MRL Dfs Research Section","MRL TCS tests","MRL Dfs Pavement Section","MRL Dfs VideoLog Users","MRL USERS","MRL Dfs Materials Section","DOTemployees","Public Com Webshare MRL"]
    mvm.put("burn1tom",burn1tomRoles)
    def wore1benRoles = ["MRL Dfs Research Section","MRL USERS","DOTemployees","OIT Web Portal Access Users","Public Com Webshare MRL","Public Com Webshare MnRoad"]
    mvm.put("wore1ben",wore1benRoles)
    def clyn1timRoles = ["MRL Dfs Research Section","MRL USERS","DOTemployees"]
    mvm.put("clyn1tim",clyn1timRoles)
    def mrlisRoles = ["MRL Administrators","MRL Dfs IS Section"]
    mvm.put("mrl-is",mrlisRoles)
    return mvm
  }
}