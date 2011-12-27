<jsec:isLoggedIn>
  Logged in as <jsec:principal/> (<g:link class="log_in_out" controller="auth" action="signOut">log out</g:link>)</div>
</jsec:isLoggedIn>

<jsec:isNotLoggedIn>
  <g:link controller="auth">Sign In</g:link>
</jsec:isNotLoggedIn>
