package us.mn.state.dot.secure;

import us.mn.state.dot.mnroad.MultiValuedMap;

import java.util.*;
import java.text.MessageFormat;

import javax.naming.*;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import javax.naming.directory.SearchControls;

public class LdapDataStore
    {
    private String connectionURL = "";
    private String connectionName = "";
    private String connectionPassword = "";
    private String contextFactory = "";
    private String referrals = "";
    private String userBase = "";
    private String userSearch = "";
    private String userRoleName;

    private String roleBase;
    private String roleSearch = "";

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getConnectionPassword() {
        return connectionPassword;
    }

    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    public String getContextFactory() {
        return contextFactory;
    }

    public void setContextFactory(String contextFactory) {
        this.contextFactory = contextFactory;
    }

    public String getReferrals() {
        return referrals;
    }

    public void setReferrals(String referrals) {
        this.referrals = referrals;
    }

    public String getUserBase() {
        return userBase;
    }

    public void setUserBase(String userBase) {
        this.userBase = userBase;
    }

    public String getUserSearch() {
        return userSearch;
    }

    public void setUserSearch(String userSearch) {
        this.userSearch = userSearch;
    }

    public String getUserRoleName() {
        return userRoleName;
    }

    public void setUserRoleName(String userRoleName) {
        this.userRoleName = userRoleName;
    }

    public String getRoleBase() {
        return roleBase;
    }

    public void setRoleBase(String roleBase) {
        this.roleBase = roleBase;
    }

    public String getRoleSearch() {
        return roleSearch;
    }

    public void setRoleSearch(String roleSearch) {
        this.roleSearch = roleSearch;
    }

    /**
     * Retrieves the distinguished name (DN) of userName from AD
     * and binds to AD to check the given credential
     *
     * @param username The user login name
     * @param userpassword The user's password
     * @return true if authentication succeeds, false if it fails
     *
     * @throws NamingException if a connection could not be established
     */
    public boolean bind(String username, String userpassword)
          throws NamingException  {
        String dn = null;
        DirContext uctx = null;
        DirContext ctx = getDirContext(connectionName, connectionPassword);
        if (ctx == null) {
            return false;
        }
        dn = getDn(username, ctx);
        if (dn != null) {
            uctx = getDirContext(dn, userpassword);
        }
        else {
            return false;
            }
        if (uctx != null) {
            uctx.close();
            return true;
        }
        return false;
    }

    /**
     * Retrieves the distinguished name (DN) of userName from AD
     *
     * @param username The user login name
     * @return distinguished name of the user
     *
     * @throws NamingException if a connection could not be established
     */
    public String getDn(String username) throws NamingException {
        DirContext ctx = getDirContext(connectionName, connectionPassword);
        if (ctx == null) {
            return null;
        }
        return getDn(username, ctx);
    }

    /**
     *
     * @param name User Name
     * @return
     * @throws CommunicationException
     * @throws NamingException
     */
    public ArrayList<String> getUserRoles(String name, boolean shortNames) throws CommunicationException, NamingException  {
        ArrayList<String> userRoles = new ArrayList<String>();
        if ((name == null) || ("".equals(name)))
            return userRoles;

        DirContext ctx = getDirContext(connectionName, connectionPassword);
        if (ctx == null)
            return userRoles;
        return getRoles(getDn(name, ctx), shortNames, ctx);
    }

    /**
     *
     * @param name User Name
     * @return AttributeList The multivalued list of directory attributes
     * @throws NamingException
     */
    public MultiValuedMap getUserAttrs(String name) throws NamingException {
        MultiValuedMap userAttrs = new MultiValuedMap();
        if ((name == null) || ("".equals(name)))
            return userAttrs;

        DirContext ctx = getDirContext(connectionName, connectionPassword);
        if (ctx == null) {
            return userAttrs;
        }
        try        {
            String distinguishedName = getDn(name, ctx);
            if (distinguishedName == null)
                return userAttrs;
            Attributes result = ctx.getAttributes(distinguishedName);
            if (result.size() > 0) {
                NamingEnumeration attrEnum = result.getAll();
                while(attrEnum.hasMore()) {
                    Attribute attr = (Attribute) attrEnum.next();
                    Enumeration attrValueEnum = attr.getAll();
                    while(attrValueEnum.hasMoreElements()) {
                        userAttrs.put(attr.getID(), (String)attrValueEnum.nextElement());
                    } // end attrValueEnum while
                }  // end attrEnum while loop
            } // end else

            ctx.close();
        } // end try
        catch (Exception e) {
            System.err.println("Search example failed.");
            e.printStackTrace();
        }

        return userAttrs;
    }


/*************************
 * Private utility methods
 *************************/
    /**
     *
     * @param cName
     * @param cPassword
     * @return Properties
     */
    private Properties getJndiConnectionProperties(String cName, String cPassword) {
        /*
        ** get the JNDI connection properties
        */
        Properties prop = new Properties();
        /*
        ** set the connection properties
        */
        prop.setProperty(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        prop.setProperty(Context.PROVIDER_URL, connectionURL);
        prop.setProperty(Context.SECURITY_PRINCIPAL, cName);
        prop.setProperty(Context.SECURITY_CREDENTIALS, cPassword);
        prop.setProperty(Context.REFERRAL, referrals);

        /*
        ** the commented out properites below are needed for accessing the LDAP
        ** server via ssl.  Also, JSSE needs to be installed and configured to
        ** support this
        */
        //prop.setProperty(Context.PROVIDER_URL, "ldap://151.111.190.xxx:636");
        //prop.setProperty(Context.SECURITY_PROTOCOL, "ssl");

        return prop;
    }

    /**
     *
     * @param cName
     * @param cPassword
     * @return
     * @throws CommunicationException
     * @throws NamingException
     */
    private DirContext getDirContext(String cName, String cPassword)
            throws NamingException  {
        DirContext ctx = null;
        Properties p = getJndiConnectionProperties(cName, cPassword);
        try {
            ctx = new InitialDirContext(p);
        } catch(Exception e) {
            if (!(e instanceof javax.naming.AuthenticationException) && !(e instanceof javax.naming.CommunicationException)) {
                e.printStackTrace();
            }
        }
        return ctx;
    }

    /**
     * Retrieves the distinguished name (DN) of userName from NDS
     *
     * @return distinguished name of the user
     */
    private String getDn(String username, DirContext ctx) {
        String dn = null;
        if (ctx == null)
            return null;
        Object[] arguments = { username };
        String userSearchCriteria = MessageFormat.format(userSearch, arguments);
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setDerefLinkFlag(true);
        try {
          NamingEnumeration results = ctx.search(userBase, userSearchCriteria, constraints);
          if ((results == null) || (!results.hasMore())) {
            /*
            ** no results found
            */
              return dn;
            }
          int nEntries = 1;

          SearchResult sr = (SearchResult) results.next();
          String rdn = sr.getName();
            /*
            ** if more than one result found, ignore all but the last
            */
          while (results.hasMore()) {
              sr = (SearchResult) results.next();
              rdn = sr.getName();
              System.out.println("RDN: " + rdn + "," + userBase);
              nEntries++;
            }
          if (nEntries > 1) { // Tomcat jndirealm aborts if more than one entry is returned.
              System.out.println("Warning - Found " + nEntries + " entries for user: " + username);
          }
          dn = rdn + "," + userBase;
          }
        catch (NamingException ne)
          {
          ne.printStackTrace();
          }
      return dn;
    }

    /**
     * Retrieves the groups associated with the given distinguished name (DN)
     *
     * @param username distinguished name used for search
     *
     * @param shortNames If true, returns the text between the '=' and the first comma
     *
     * @return an ArrayList of group names
     */
    private ArrayList<String> getRoles(String username, boolean shortNames, DirContext ctx) {
        ArrayList<String> roles = new ArrayList<String>();

        Object[] arguments = { username };
        String userSearchCriteria = MessageFormat.format(roleSearch, arguments);
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setDerefLinkFlag(true);
        try {
          NamingEnumeration results = ctx.search(roleBase, userSearchCriteria, constraints);
          if ((results == null) || (!results.hasMore())) {
            /*
            ** no results found
            */
              return roles;
            }
            SearchResult sr = null;
            while (results.hasMore()) {
              sr = (SearchResult) results.next();
              if (shortNames)
                roles.add(roleFromRoleDn(sr.getName()).trim());
              else
                roles.add(sr.getName() + "," + roleBase);
            }
        }  catch (NamingException ne)  {
        ne.printStackTrace();
        }
        return roles;
    }

    /**
    * Extract the text between the first "=" and the next comma
    * Strip off the "CN=" and every character after the first comma
    * @param dn
    * @return
    */
    private String roleFromRoleDn(String dn)  {
        String role = dn.substring(dn.indexOf('=') + 1, dn.length());
        StringTokenizer st = new StringTokenizer(role, ",");
        role = (String) st.nextElement();
        return role;
    }
	
/*
Change password snippet - untested
		Hashtable env = new Hashtable();
		String userName = "CN=Albert Einstein,OU=Research,DC=antipodes,DC=com";
		String oldPassword = "Password123";
		String newPassword = "456P@ssw0rd";
		
		//Access the keystore, this is where the Root CA public key cert was installed
		//Could also do this via command line java -Djavax.net.ssl.trustStore....
		String keystore = "/usr/java/jdk1.5.0_01/jre/lib/security/cacerts";
		System.setProperty("javax.net.ssl.trustStore",keystore);
 
		env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
 
		//set security credentials, note using simple cleartext authentication
		env.put(Context.SECURITY_AUTHENTICATION,"simple");
		env.put(Context.SECURITY_PRINCIPAL,userName);
		env.put(Context.SECURITY_CREDENTIALS,oldPassword);
				
		//specify use of ssl
		env.put(Context.SECURITY_PROTOCOL,"ssl");
 
		//connect to my domain controller
		String ldapURL = "ldaps://mydc.antipodes.com:636";
		env.put(Context.PROVIDER_URL,ldapURL);
		
		try {
 
			// Create the initial directory context
			LdapContext ctx = new InitialLdapContext(env,null);
	    
			//change password is a single ldap modify operation
			//that deletes the old password and adds the new password
			ModificationItem[] mods = new ModificationItem[2];
		
 
			//Firstly delete the "unicdodePwd" attribute, using the old password
			//Then add the new password,Passwords must be both Unicode and a quoted string
			String oldQuotedPassword = "\"" + oldPassword + "\"";
			byte[] oldUnicodePassword = oldQuotedPassword.getBytes("UTF-16LE");
			String newQuotedPassword = "\"" + newPassword + "\"";
			byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
		
			mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute("unicodePwd", oldUnicodePassword));
			mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("unicodePwd", newUnicodePassword));
 
			// Perform the update
			ctx.modifyAttributes(userName, mods);
		
			System.out.println("Changed Password for: " + userName);	
			ctx.close();
 
		} 
		catch (NamingException e) {
			System.err.println("Problem changing password: " + e);
		}
		catch (UnsupportedEncodingException e) {
			System.err.println("Problem encoding password: " + e);
		}

*/	
} // end class

