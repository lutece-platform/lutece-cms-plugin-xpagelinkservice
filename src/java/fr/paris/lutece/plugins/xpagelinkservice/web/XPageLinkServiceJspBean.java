/*
 * Copyright (c) 2002-2017, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.xpagelinkservice.web;

import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.content.XPageAppService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.security.ISecurityTokenService;
import fr.paris.lutece.portal.service.security.SecurityTokenHandler;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.portal.web.insert.InsertServiceJspBean;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectionBean;
import fr.paris.lutece.portal.web.xpages.XPageApplicationEntry;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;


/**
 * Insert service that inserts into a rich text editor a link to an application (XPage) of the site. The core builds it by reflection.
 */
public class XPageLinkServiceJspBean extends InsertServiceJspBean implements InsertServiceSelectionBean
{
    private static final long serialVersionUID = 1L;

    private static final List<String> LIST_TARGETS = List.of( "_self", "_blank" );

    private static final String TEMPLATE_SELECTOR_PAGE = "admin/plugins/xpagelinkservice/xpagelinkservice_selector.html";
    private static final String TEMPLATE_LINK = "admin/plugins/xpagelinkservice/xpagelinkservice_link.html";

    private static final String PARAMETER_SELECTED_TEXT = "selected_text";
    private static final String PARAMETER_TEXT = "text";
    private static final String PARAMETER_TITLE = "title";
    private static final String PARAMETER_TARGET = "target";
    private static final String PARAMETER_XPAGE = "xpage";
    private static final String PARAMETER_INPUT = "input";

    private static final String MARK_APPLICATIONS_LIST = "applications_list";
    private static final String MARK_SELECTED_TEXT = "selected_text";
    private static final String MARK_INPUT = "input";
    private static final String MARK_URL = "url";
    private static final String MARK_TEXT = "text";
    private static final String MARK_TITLE = "title";
    private static final String MARK_TARGET = "target";

    /**
     * Returns the html form that selects the application to link to.
     *
     * @param request The Http Request
     * @return The html form.
     */
    @Override
    public String getInsertServiceSelectorUI( HttpServletRequest request )
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_APPLICATIONS_LIST, getEnabledApplications( ) );
        model.put( MARK_SELECTED_TEXT, request.getParameter( PARAMETER_SELECTED_TEXT ) );
        model.put( MARK_INPUT, request.getParameter( PARAMETER_INPUT ) );
        model.put( SecurityTokenHandler.MARK_CSRF_TOKEN, CDI.current( ).select( ISecurityTokenService.class ).get( )
                .getToken( request, XPageLinkServiceSelectorJspBean.ACTION_INSERT_XPAGE_LINK ) );

        Locale locale = AdminUserService.getLocale( request );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_SELECTOR_PAGE, locale, model );

        return template.getHtml( );
    }

    /**
     * Builds the link to the selected application and returns the url that inserts it into the calling editor field.
     *
     * @param request The http request
     * @return the insertion url, or the url of an error message when a mandatory field is missing or the application is unknown
     */
    public String getInsertLinkUrl( HttpServletRequest request )
    {
        String strText = request.getParameter( PARAMETER_TEXT );
        String strTitle = request.getParameter( PARAMETER_TITLE );
        String strXPage = request.getParameter( PARAMETER_XPAGE );

        if ( StringUtils.isAnyBlank( strText, strTitle ) || !isEnabledApplication( strXPage ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        String strTarget = request.getParameter( PARAMETER_TARGET );

        UrlItem urlXPage = new UrlItem( AppPathService.getPortalUrl( ) );
        urlXPage.addParameter( XPageAppService.PARAM_XPAGE_APP, strXPage );

        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_URL, urlXPage.getUrl( ) );
        model.put( MARK_TARGET, LIST_TARGETS.contains( strTarget ) ? strTarget : StringUtils.EMPTY );
        model.put( MARK_TITLE, toHtmlText( strTitle ) );
        model.put( MARK_TEXT, toHtmlText( strText ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_LINK, AdminUserService.getLocale( request ), model );

        return insertUrl( request, request.getParameter( PARAMETER_INPUT ), StringEscapeUtils.escapeEcmaScript( template.getHtml( ) ) );
    }

    /**
     * Returns the applications of the enabled plugins, sorted by id.
     *
     * @return the enabled applications
     */
    private static List<XPageApplicationEntry> getEnabledApplications( )
    {
        return XPageAppService.getXPageApplicationsList( ).stream( ).filter( XPageApplicationEntry::isEnable )
                .sorted( Comparator.comparing( XPageApplicationEntry::getId ) ).toList( );
    }

    /**
     * Tells whether an id names an application of an enabled plugin.
     *
     * @param strXPage the application id
     * @return true if the application exists and is enabled
     */
    private static boolean isEnabledApplication( String strXPage )
    {
        return getEnabledApplications( ).stream( ).anyMatch( entry -> entry.getId( ).equals( strXPage ) );
    }

    /**
     * Escapes a text for HTML once, whether or not the core XSS filter already encoded some of its characters.
     *
     * @param strText the text
     * @return the escaped text
     */
    private static String toHtmlText( String strText )
    {
        return StringEscapeUtils.escapeHtml4( StringEscapeUtils.unescapeHtml4( strText ) );
    }
}
