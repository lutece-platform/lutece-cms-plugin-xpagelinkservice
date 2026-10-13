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

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.portal.service.content.XPageAppService;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectorJspBean;
import fr.paris.lutece.portal.web.xpages.XPageApplicationEntry;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;

/**
 * Tests the link built by the application insert service.
 */
public class XPageLinkServiceJspBeanTest extends LuteceTestCase
{
    private static final String URL_INSERT = "jsp/admin/insert/DoInsertIntoElement.jsp";
    private static final String URL_MESSAGE = "AdminMessage.jsp";
    private static final String PLUGIN_CORE = "core";

    private String _strXPage;
    private MockHttpServletRequest _request;

    /**
     * Registers an enabled application and prepares a complete request.
     *
     * @throws Exception
     *             if the test context fails to start
     */
    @BeforeEach
    protected void setUp( ) throws Exception
    {
        super.setUp( );
        _strXPage = "xpagelinktest" + UUID.randomUUID( ).toString( ).substring( 0, 8 );
        XPageApplicationEntry entry = new XPageApplicationEntry( );
        entry.setId( _strXPage );
        entry.setClassName( Object.class.getName( ) );
        entry.setPluginName( PLUGIN_CORE );
        entry.setEnabled( true );
        XPageAppService.registerXPageApplication( entry );

        _request = new MockHttpServletRequest( );
        _request.addParameter( "text", "Plan & co" );
        _request.addParameter( "title", "Le \"plan\"" );
        _request.addParameter( "target", "_blank" );
        _request.addParameter( "xpage", _strXPage );
        _request.addParameter( "input", "tinymce" );
    }

    /**
     * A complete request inserts a link to the application, its text and title escaped once.
     */
    @Test
    public void testInsertLink( )
    {
        String strUrl = new XPageLinkServiceJspBean( ).getInsertLinkUrl( _request );
        String strLink = getInsertedLink( );

        assertTrue( strUrl.contains( URL_INSERT ), strUrl );
        assertTrue( strLink.contains( "page=" + _strXPage ), strLink );
        assertTrue( strLink.contains( "target=\\\"_blank\\\"" ), strLink );
        assertTrue( strLink.contains( ">Plan &amp; co<\\/a>" ), strLink );
        assertTrue( strLink.contains( "title=\\\"Le &quot;plan&quot;\\\"" ), strLink );
    }

    /**
     * A target outside the allowed values is dropped from the link.
     */
    @Test
    public void testUnknownTargetIsDropped( )
    {
        _request.setParameter( "target", "javascript:alert(1)" );

        new XPageLinkServiceJspBean( ).getInsertLinkUrl( _request );

        assertFalse( getInsertedLink( ).contains( "target=" ), getInsertedLink( ) );
    }

    /**
     * An unknown application or a missing title inserts nothing and shows an error message.
     */
    @Test
    public void testInvalidRequestInsertsNothing( )
    {
        _request.setParameter( "xpage", "unknown-" + _strXPage );
        assertTrue( new XPageLinkServiceJspBean( ).getInsertLinkUrl( _request ).contains( URL_MESSAGE ) );

        _request.setParameter( "xpage", _strXPage );
        _request.setParameter( "title", " " );
        assertTrue( new XPageLinkServiceJspBean( ).getInsertLinkUrl( _request ).contains( URL_MESSAGE ) );

        assertNull( _request.getSession( ).getAttribute( InsertServiceSelectorJspBean.SESSION_INSERT ) );
    }

    /**
     * Returns the link the insert service stored for the editor.
     *
     * @return the stored link
     */
    private String getInsertedLink( )
    {
        return (String) _request.getSession( ).getAttribute( InsertServiceSelectorJspBean.SESSION_INSERT );
    }
}
