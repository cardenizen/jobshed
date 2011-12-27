import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.NamingException
//import javax.naming.directory.BasicAttribute
//import javax.naming.directory.BasicAttributes
import javax.naming.directory.InitialDirContext

import org.jsecurity.authc.AccountException
import org.jsecurity.authc.CredentialsException
import org.jsecurity.authc.IncorrectCredentialsException
//import org.jsecurity.authc.UnknownAccountException
import org.jsecurity.authc.SimpleAccount
import us.mn.state.dot.secure.AdDirectory
import us.mn.state.dot.secure.Ad
import org.apache.log4j.Logger

/**
 * Simple realm that authenticates users against an LDAP server.
 */
class JsecLdapRealm {
    static Logger log = Logger.getLogger(JsecLdapRealm.class)
    static authTokenClass = org.jsecurity.authc.UsernamePasswordToken
    // Strip off the "CN=" and every character after the first comma of the DN
    static boolean RETURN_SHORT_ROLE_NAMES = true

    def grailsApplication

    def authenticate(authToken) {
    
        log.info "Attempting to authenticate ${authToken.username} in LDAP realm..."
        def username = authToken.username
        def password = new String(authToken.password)

        // Get LDAP config for application. Use defaults when no config
        // is provided.
        def appConfig = grailsApplication.config
        def ldapUrls = appConfig.ldap.server.url ?: [ "ldap://ldapad:389/" ]
        def searchBase = appConfig.ldap.search.base ?: "DC=ad,DC=dot,DC=state,DC=mn,DC=us"
        def searchUser = appConfig.ldap.search.user ?: "ldapbrowse"
        def searchPass = appConfig.ldap.search.pass ?: "ldapbrowse"
		def usernameAttribute = appConfig.ldap.username.attribute ?: "sAMAccountName"
        def skipAuthc = appConfig.ldap.skip.authentication ?: false
        def skipCredChk = appConfig.ldap.skip.credentialsCheck ?: false
        def allowEmptyPass = appConfig.ldap.allowEmptyPasswords != [:] ? appConfig.ldap.allowEmptyPasswords : false

        // Skip authentication ?
        if (skipAuthc) {
          log.info "Skipping authentication in development mode."
          return username
        }

        // Null username is invalid
        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.")
        }

        // Empty username is invalid
        if (username == "") {
            throw new AccountException("Empty usernames are not allowed by this realm.")
        }

        // Allow empty passwords ?
        if (!allowEmptyPass) {
            // Null password is invalid
            if (password == null) {
                throw new CredentialsException("Null password are not allowed by this realm.")
            }

            // empty password is invalid
            if (password == "") {
                throw new CredentialsException("Empty passwords are not allowed by this realm.")
            }
        }

        // Accept strings and GStrings for convenience, but convert to
        // a list.
        if (ldapUrls && !(ldapUrls instanceof Collection)) {
            ldapUrls = [ ldapUrls ]
        }

        // Skip credentials check ?
        if (skipCredChk) {
          log.info "Skipping credentials check in development mode."
          return username
        }
//return username
        // Set up the configuration for the LDAP search we are about
        // to do.
        def env = new Hashtable()
        env[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
        if (searchUser) {
            // Non-anonymous access for the search.
            env[Context.SECURITY_AUTHENTICATION] = "simple"
            env[Context.SECURITY_PRINCIPAL] = searchUser
            env[Context.SECURITY_CREDENTIALS] = searchPass
        }

        // Find an LDAP server that we can connect to.
        def ctx
        def urlUsed = ldapUrls.find { url ->
            log.info "Trying LDAP server ${url} ..."
            env[Context.PROVIDER_URL] = url

            // If an exception occurs, log it.
            try {
                ctx = new InitialDirContext(env)
                return true
            }
            catch (NamingException e) {
                log.error "Could not connect to ${url}: ${e}"
                return false
            }
        }

        if (!urlUsed) {
            def msg = 'No LDAP server available.'
            log.error msg
            throw new org.jsecurity.authc.AuthenticationException(msg)
        }


        // Skip credentials check ?
        if (skipCredChk) {
          log.info "Skipping credentials check in development mode."
          return username
        }

        AdDirectory ad = new Ad()
        ad.configure(grailsApplication.config)
        String user = authToken.username.toString()

        String userDn = ad.findUser(user)
        if (!userDn) {
          return username
        }

        // Now connect to the LDAP server again, but this time use
        // authentication with the principal associated with the given
        // username.
        try {
            ad.authenticate(user,password)
            log.info "Found user ${username} in LDAP realm..."
            return username
        }
        catch (AuthenticationException ex) {
            log.info "Invalid password"
            throw new IncorrectCredentialsException("Invalid password for user '${username}'")
        }

    } // authenticate


    def hasRole(principal, roleName) {
      if (!principal)
        return false
      AdDirectory ad = new Ad()
      ad.configure(grailsApplication.config)
      ArrayList roles = ad.getUserRoles(principal, RETURN_SHORT_ROLE_NAMES)
      //log.info "User role(s) for '${principal}': ${roles}"
      return roles.contains(roleName)
    }
}
