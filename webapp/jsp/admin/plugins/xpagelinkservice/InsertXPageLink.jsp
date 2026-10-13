<%@ page errorPage="../../ErrorPage.jsp" %>

${ pageContext.setAttribute( 'strContent', xPageLinkServiceSelectorJspBean.processController( pageContext.request , pageContext.response ) ) }

<jsp:include page="../../insert/InsertServiceHeader.jsp" />

${ pageContext.getAttribute( 'strContent' ) }
</div>
</body>
</html>
