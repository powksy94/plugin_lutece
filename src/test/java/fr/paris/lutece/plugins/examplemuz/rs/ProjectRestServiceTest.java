/*
 * Copyright (c) 2002-2026, City of Paris
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
package fr.paris.lutece.plugins.examplemuz.rs;

import fr.paris.lutece.plugins.examplemuz.business.Project;
import fr.paris.lutece.plugins.examplemuz.business.ProjectHome;
import fr.paris.lutece.test.LuteceTestCase;

import javax.ws.rs.core.Response;

/**
 * This is the REST service test for the object Project
 */
public class ProjectRestServiceTest extends LuteceTestCase 
{
    private static final String NAME1 = "Name1";
    private static final String DESCRIPTION1 ="Description1";
    private static final String IMAGEURL1 ="http://imageurl1.com";
    private static final int COST1 = 10;

    public void testGetProjects ( )
    {
        ProjectRestService service = new ProjectRestService( );

        Project project = new Project( );
        project.setName( NAME1 );
        project.setDescription(DESCRIPTION1);
        project.setImageUrl(IMAGEURL1);
        project.setCost(COST1);
        ProjectHome.create(project);

        String strJson = service.getProjects();
        assertNotNull(strJson);
        assertTrue( strJson.contains("projects") );
        assertTrue( strJson.contains( NAME1 ) );

        ProjectHome.remove( project.getId( ) );
    }

    public void testGetProject( )
    {
        ProjectRestService service = new ProjectRestService( );

        Project project = new Project( );
        project.setName(NAME1);
        project.setDescription(DESCRIPTION1);
        project.setImageUrl(IMAGEURL1);
        project.setCost(COST1);
        ProjectHome.create(project);

        Response response = service.getProject( String.valueOf( project.getId( ) ) );
        assertEquals( Response.Status.OK.getStatusCode( ), response.getStatus( ) );
        assertTrue( response.getEntity( ).toString( ).contains( NAME1 ) );

        ProjectHome.remove( project.getId( ) );
    }

    public void testGetProjectNotFound( )
    {
        ProjectRestService service = new ProjectRestService( );
        Response response = service.getProject( "99999" );
        assertEquals( Response.Status.NOT_FOUND.getStatusCode( ), response.getStatus( ) );
    }

    public void testGetProjectInvalidId( )
    {
        ProjectRestService service = new ProjectRestService( );
        Response response = service.getProject( "abc" );
        assertEquals( Response.Status.BAD_REQUEST.getStatusCode( ), response.getStatus( ) );
    }
}
