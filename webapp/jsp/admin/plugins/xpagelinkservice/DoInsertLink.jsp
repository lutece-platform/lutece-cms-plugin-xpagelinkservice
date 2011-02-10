<%@ page errorPage="ErrorPage.jsp" %>

<jsp:useBean id="xpagels" scope="session" class="fr.paris.lutece.plugins.xpagelinkservice.web.XPageLinkServiceJspBean" />

<% 
     response.sendRedirect( xpagels.doInsertLink( request ) );
%>
