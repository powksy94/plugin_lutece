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
