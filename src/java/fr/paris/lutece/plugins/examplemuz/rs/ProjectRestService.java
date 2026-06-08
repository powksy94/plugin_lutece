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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path( "/examplemuz" )
public class ProjectRestService
{
    @GET
    @Path( "/projects" )
    @Produces( MediaType.APPLICATION_JSON )
    public String getProjects( )
    {
        ObjectMapper mapper = new ObjectMapper( );
        ObjectNode json = mapper.createObjectNode( );
        ArrayNode jsonProjects = mapper.createArrayNode( );

        for ( Project project : ProjectHome.getProjectsList( ) )
        {
            ObjectNode jsonProject = mapper.createObjectNode( );
            jsonProject.put( "id", project.getId( ) );
            jsonProject.put( "name", project.getName( ) );
            jsonProject.put( "description", project.getDescription( ) );
            jsonProject.put( "cost", project.getCostInEuros( ) );
            jsonProjects.add( jsonProject );
        }
        json.set( "projects", jsonProjects );
        return json.toString( );
    }

    @GET
    @Path( "/projects/{id}" )
    @Produces( MediaType.APPLICATION_JSON )
    public String getProject( @PathParam( "id" ) int nId )
    {
        String strKey = "project_" + nId;

        String cachedResult = (String) ProjectCacheService.getInstance( ).getFromCache( strKey );
        if ( cachedResult != null )
        {
            return cachedResult;
        }

        java.util.Optional<Project> optProject = ProjectHome.findByPrimaryKey( nId );
        if ( !optProject.isPresent( ) )
        {
            return "{}";
        }
        Project project = optProject.get( );
        ObjectMapper mapper = new ObjectMapper( );
        ObjectNode json = mapper.createObjectNode( );
        json.put( "id", project.getId( ) );
        json.put( "name", project.getName( ) );
        json.put( "description", project.getDescription( ) );
        json.put( "cost", project.getCostInEuros( ) );

        String strResult = json.toString( );
        ProjectCacheService.getInstance( ).putInCache( strKey, strResult );
        return strResult;
    }
}
