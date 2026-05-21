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
 	
 
package fr.paris.lutece.plugins.examplemuz.web;

import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;
import fr.paris.lutece.util.html.AbstractPaginator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;


import fr.paris.lutece.plugins.examplemuz.business.Project;
import fr.paris.lutece.plugins.examplemuz.business.ProjectHome;

/**
 * This class provides the user interface to manage Project features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageProjects.jsp", controllerPath = "jsp/admin/plugins/examplemuz/", right = "PROJECT_MANAGEMENT" )
public class ProjectJspBean extends AbstractJspBean <Integer, Project>
{

	// Rights
	public static final String RIGHT_MANAGEPROJECT = "PROJECT_MANAGEMENT";
		
    // Templates
    private static final String TEMPLATE_MANAGE_PROJECTS = "/admin/plugins/examplemuz/manage_projects.html";
    private static final String TEMPLATE_CREATE_PROJECT = "/admin/plugins/examplemuz/create_project.html";
    private static final String TEMPLATE_MODIFY_PROJECT = "/admin/plugins/examplemuz/modify_project.html";

    // Parameters
    private static final String PARAMETER_ID_PROJECT = "id";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_PROJECTS = "examplemuz.manage_projects.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_PROJECT = "examplemuz.modify_project.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_PROJECT = "examplemuz.create_project.pageTitle";

    // Markers
    private static final String MARK_PROJECT_LIST = "project_list";
    private static final String MARK_PROJECT = "project";

    private static final String JSP_MANAGE_PROJECTS = "jsp/admin/plugins/examplemuz/ManageProjects.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_PROJECT = "examplemuz.message.confirmRemoveProject";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "examplemuz.model.entity.project.attribute.";

    // Views
    private static final String VIEW_MANAGE_PROJECTS = "manageProjects";
    private static final String VIEW_CREATE_PROJECT = "createProject";
    private static final String VIEW_MODIFY_PROJECT = "modifyProject";

    // Actions
    private static final String ACTION_CREATE_PROJECT = "createProject";
    private static final String ACTION_MODIFY_PROJECT = "modifyProject";
    private static final String ACTION_REMOVE_PROJECT = "removeProject";
    private static final String ACTION_CONFIRM_REMOVE_PROJECT = "confirmRemoveProject";

    // Infos
    private static final String INFO_PROJECT_CREATED = "examplemuz.info.project.created";
    private static final String INFO_PROJECT_UPDATED = "examplemuz.info.project.updated";
    private static final String INFO_PROJECT_REMOVED = "examplemuz.info.project.removed";
    
    // Errors
    private static final String ERROR_RESOURCE_NOT_FOUND = "Resource not found";
    
    // Session variable to store working values
    private Project _project;
    private List<Integer> _listIdProjects;
    private HashMap<String,String> _mapFilterCriteria = new HashMap<>();
    private String _optionOrderBy;
    
    /**
     * Build the Manage View
     * @param request The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_PROJECTS, defaultView = true )
    public String getManageProjects( HttpServletRequest request )
    {
        _project = null;
        
        // new search only if in pagination mode
        if ( request.getParameter( AbstractPaginator.PARAMETER_PAGE_INDEX) == null )
        {
        	// if sorting request : new search with the existing filter criteria, ordered 
        	// example of order by parameter : orderby=name
        	if ( StringUtils.isNotBlank( (String)request.getParameter(PARAMETER_SEARCH_ORDER_BY) ) )
        	{
        		
        		String strOrderByColumn =  (String)request.getParameter(PARAMETER_SEARCH_ORDER_BY);
        		String strSortMode = getSortMode(); 
        		
        		_listIdProjects = ProjectHome.getIdProjectsList( _mapFilterCriteria, strOrderByColumn, strSortMode );
               	
	       	}
	       	else
	       	{
	       		// reload the filter criteria and search
	       		_mapFilterCriteria = (HashMap<String, String>) getFilterCriteriaFromRequest( request );
	       		_listIdProjects = ProjectHome.getIdProjectsList( _mapFilterCriteria, null ,null);
	       	}
        	
        	//set CurrentPageIndex of Paginator to null in aim of displays the first page of results
        	resetCurrentPageIndexOfPaginator();
        }
       	
       	Map<String, Object> model = getPaginatedListModel( request, MARK_PROJECT_LIST, _listIdProjects, JSP_MANAGE_PROJECTS );
             
        addSearchParameters(model,_mapFilterCriteria); //allow the persistence of search values in inputs search bar inputs
                     
        return getPage( PROPERTY_PAGE_TITLE_MANAGE_PROJECTS, TEMPLATE_MANAGE_PROJECTS, model );

    }

	/**
     * Get Items from Ids list
     * @param listIds
     * @return the populated list of items corresponding to the id List
     */
	@Override
	List<Project> getItemsFromIds( List<Integer> listIds ) 
	{
		List<Project> listProject = ProjectHome.getProjectsListByIds( listIds );
		
		// keep original order
        return listProject.stream()
                 .sorted(Comparator.comparingInt( notif -> listIds.indexOf( notif.getId())))
                 .collect(Collectors.toList());
	}
	
	@Override
	int getPluginDefaultNumberOfItemPerPage( ) {
		return AppPropertiesService.getPropertyInt( PROPERTY_DEFAULT_LIST_ITEM_PER_PAGE, 50 );
	}
    
    /**
    * reset the _listIdProjects list
    */
    public void resetListId( )
    {
    	_listIdProjects = new ArrayList<>( );
    }

    /**
     * Returns the form to create a project
     *
     * @param request The Http request
     * @return the html code of the project form
     */
    @View( VIEW_CREATE_PROJECT )
    public String getCreateProject( HttpServletRequest request )
    {
        _project = ( _project != null ) ? _project : new Project(  );

        Map<String, Object> model = getModel(  );
        model.put( MARK_PROJECT, _project );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_CREATE_PROJECT ) );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_PROJECT, TEMPLATE_CREATE_PROJECT, model );
    }

    /**
     * Process the data capture form of a new project
     *
     * @param request The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_CREATE_PROJECT )
    public String doCreateProject( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _project, request, getLocale( ) );
        

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_CREATE_PROJECT ) )
        {
            throw new AccessDeniedException ( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _project, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_PROJECT );
        }

        ProjectHome.create( _project );
        addInfo( INFO_PROJECT_CREATED, getLocale(  ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_PROJECTS );
    }

    /**
     * Manages the removal form of a project whose identifier is in the http
     * request
     *
     * @param request The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_PROJECT )
    public String getConfirmRemoveProject( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_PROJECT ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_PROJECT ) );
        url.addParameter( PARAMETER_ID_PROJECT, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_PROJECT, url.getUrl(  ), AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a project
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage projects
     */
    @Action( ACTION_REMOVE_PROJECT )
    public String doRemoveProject( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_PROJECT ) );
        
        
        ProjectHome.remove( nId );
        addInfo( INFO_PROJECT_REMOVED, getLocale(  ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_PROJECTS );
    }

    /**
     * Returns the form to update info about a project
     *
     * @param request The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_PROJECT )
    public String getModifyProject( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_PROJECT ) );

        if ( _project == null || ( _project.getId(  ) != nId ) )
        {
            Optional<Project> optProject = ProjectHome.findByPrimaryKey( nId );
            _project = optProject.orElseThrow( ( ) -> new AppException(ERROR_RESOURCE_NOT_FOUND ) );
        }


        Map<String, Object> model = getModel(  );
        model.put( MARK_PROJECT, _project );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_PROJECT ) );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_PROJECT, TEMPLATE_MODIFY_PROJECT, model );
    }

    /**
     * Process the change form of a project
     *
     * @param request The Http request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_MODIFY_PROJECT )
    public String doModifyProject( HttpServletRequest request ) throws AccessDeniedException
    {   
        populate( _project, request, getLocale( ) );
		
		
        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_MODIFY_PROJECT ) )
        {
            throw new AccessDeniedException ( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _project, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_PROJECT, PARAMETER_ID_PROJECT, _project.getId( ) );
        }

        ProjectHome.update( _project );
        addInfo( INFO_PROJECT_UPDATED, getLocale(  ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_PROJECTS );
    }
}
