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

import fr.paris.lutece.plugins.examplemuz.business.Project;
import fr.paris.lutece.plugins.examplemuz.business.ProjectHome;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.portal.util.mvc.xpage.MVCApplication;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.xpage.annotations.Controller;
import fr.paris.lutece.util.url.UrlItem;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.util.AppException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest; 


/**
 * This class provides the user interface to manage Project xpages ( manage, create, modify, remove )
 */
@Controller( xpageName = "project" , pageTitleI18nKey = "examplemuz.xpage.project.pageTitle" , pagePathI18nKey = "examplemuz.xpage.project.pagePathLabel" )
public class ProjectXPage extends MVCApplication
{
    // Templates
    private static final String TEMPLATE_MANAGE_PROJECTS = "/skin/plugins/examplemuz/manage_projects.html";
    private static final String TEMPLATE_CREATE_PROJECT = "/skin/plugins/examplemuz/create_project.html";
    private static final String TEMPLATE_MODIFY_PROJECT = "/skin/plugins/examplemuz/modify_project.html";
    
    // Parameters
    private static final String PARAMETER_ID_PROJECT = "id";
    
    // Markers
    private static final String MARK_PROJECT_LIST = "project_list";
    private static final String MARK_PROJECT = "project";
    
    // Message
    private static final String MESSAGE_CONFIRM_REMOVE_PROJECT = "examplemuz.message.confirmRemoveProject";
    
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
    
    /**
     * return the form to manage projects
     * @param request The Http request
     * @return the html code of the list of projects
     */
    @View( value = VIEW_MANAGE_PROJECTS, defaultView = true )
    public XPage getManageProjects( HttpServletRequest request )
    {
        _project = null;
        List<Project> listProjects = ProjectHome.getProjectsList(  );
        
        Map<String, Object> model = getModel(  );
        model.put( MARK_PROJECT_LIST, listProjects );
        
        return getXPage( TEMPLATE_MANAGE_PROJECTS, getLocale( request ), model );
    }

    /**
     * Returns the form to create a project
     *
     * @param request The Http request
     * @return the html code of the project form
     */
    @View( VIEW_CREATE_PROJECT )
    public XPage getCreateProject( HttpServletRequest request )
    {
        _project = ( _project != null ) ? _project : new Project(  );

        Map<String, Object> model = getModel(  );
        model.put( MARK_PROJECT, _project );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_CREATE_PROJECT ) );

        return getXPage( TEMPLATE_CREATE_PROJECT, getLocale( request ), model );
    }

    /**
     * Process the data capture form of a new project
     *
     * @param request The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_CREATE_PROJECT )
    public XPage doCreateProject( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _project, request, getLocale( request ) );

		
        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_CREATE_PROJECT ) )
        {
            throw new AccessDeniedException ( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _project ) )
        {
            return redirectView( request, VIEW_CREATE_PROJECT );
        }

        ProjectHome.create( _project );
        addInfo( INFO_PROJECT_CREATED, getLocale( request ) );

        return redirectView( request, VIEW_MANAGE_PROJECTS );
    }

    /**
     * Manages the removal form of a project whose identifier is in the http
     * request
     *
     * @param request The Http request
     * @return the html code to confirm
     * @throws fr.paris.lutece.portal.service.message.SiteMessageException {@link fr.paris.lutece.portal.service.message.SiteMessageException}
     */
    @Action( ACTION_CONFIRM_REMOVE_PROJECT )
    public XPage getConfirmRemoveProject( HttpServletRequest request ) throws SiteMessageException
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_PROJECT ) );

        UrlItem url = new UrlItem( getActionFullUrl( ACTION_REMOVE_PROJECT ) );
        url.addParameter( PARAMETER_ID_PROJECT, nId );
        
        SiteMessageService.setMessage( request, MESSAGE_CONFIRM_REMOVE_PROJECT, SiteMessage.TYPE_CONFIRMATION, url.getUrl(  ) );
        return null;
    }

    /**
     * Handles the removal form of a project
     *
     * @param request The Http request
     * @return the jsp URL to display the form to manage projects
     */
    @Action( ACTION_REMOVE_PROJECT )
    public XPage doRemoveProject( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_PROJECT ) );
        ProjectHome.remove( nId );
        addInfo( INFO_PROJECT_REMOVED, getLocale( request ) );

        return redirectView( request, VIEW_MANAGE_PROJECTS );
    }

    /**
     * Returns the form to update info about a project
     *
     * @param request The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_PROJECT )
    public XPage getModifyProject( HttpServletRequest request )
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

        return getXPage( TEMPLATE_MODIFY_PROJECT, getLocale( request ), model );
    }

    /**
     * Process the change form of a project
     *
     * @param request The Http request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_MODIFY_PROJECT )
    public XPage doModifyProject( HttpServletRequest request ) throws AccessDeniedException
    {     
        populate( _project, request, getLocale( request ) );
		

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_MODIFY_PROJECT ) )
        {
            throw new AccessDeniedException ( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _project ) )
        {
            return redirect( request, VIEW_MODIFY_PROJECT, PARAMETER_ID_PROJECT, _project.getId( ) );
        }

        ProjectHome.update( _project );
        addInfo( INFO_PROJECT_UPDATED, getLocale( request ) );

        return redirectView( request, VIEW_MANAGE_PROJECTS );
    }
}
