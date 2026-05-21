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

import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.html.AbstractPaginator;
import fr.paris.lutece.util.url.UrlItem;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractJspBean <S, T> extends MVCAdminJspBean
{
    
    // Properties
    protected static final String PROPERTY_DEFAULT_LIST_ITEM_PER_PAGE = "examplemuz.listItems.itemsPerPage";
    private static final int PROPERTY_DEFAULT_ITEM_PER_PAGE = 50;
    
    // Parameters
    private static final String PARAMETER_PAGE_INDEX = "page_index";
    protected static final String PARAMETER_SEARCH_ORDER_BY = "orderBy";
    private static final String  PARAMETER_MAP_FILTER_CRITERIA = "mapFilterCriteria";  
    
    // Markers
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";

    //Search
    private static final String FILTER_ATTRIBUTES_PREFIX = "filter_";
    private static final String SORT_ATTRIBUTES_ASC = " ASC ";
    private static final String SORT_ATTRIBUTES_DESC = " DESC ";
    
    //Variables
    private String _strCurrentPageIndex;
    private int _nItemsPerPage;
    private String _strSortMode="";

    /**
     * Return a model that contains the list and paginator infos
     * @param request The HTTP request
     * @param strBookmark The bookmark
     * @param list The list of item
     * @param strManageJsp The JSP
     * @return The model
     */
    protected <T> Map<String, Object> getPaginatedListModel( HttpServletRequest request, String strBookmark, List<S> list,
        String strManageJsp )
    {
        int nDefaultItemsPerPage = getPluginDefaultNumberOfItemPerPage( );
        _strCurrentPageIndex = AbstractPaginator.getPageIndex( request, AbstractPaginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nItemsPerPage = AbstractPaginator.getItemsPerPage( request, AbstractPaginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, nDefaultItemsPerPage );

        UrlItem url = new UrlItem( strManageJsp );
        String strUrl = url.getUrl(  );

        // PAGINATOR
        LocalizedPaginator<S> paginator = new LocalizedPaginator<>( list, _nItemsPerPage, strUrl, PARAMETER_PAGE_INDEX, _strCurrentPageIndex, getLocale(  ) );

        Map<String, Object> model = getModel(  );

        model.put( MARK_NB_ITEMS_PER_PAGE, String.valueOf( _nItemsPerPage ) );
        model.put( MARK_PAGINATOR, paginator );
        model.put( strBookmark, getItemsFromIds ( paginator.getPageItems( ) ) );

        return model;
    }
    
    /**
     * Get Items from Ids list
     * @param <T>
     *
     * @param <S> the generic type of the Ids
     * @param <T> the generic type of the items
     * @param <S>
     * @param listIds
     * @return the populated list of items corresponding to the id List
     */
     abstract  List<T> getItemsFromIds ( List<S> listIds ) ;
     
     int getPluginDefaultNumberOfItemPerPage( ) { return PROPERTY_DEFAULT_ITEM_PER_PAGE; } ;
     
      /**
      * set _strCurrentPageIndex to null
      */
     protected void resetCurrentPageIndexOfPaginator() {
    	 _strCurrentPageIndex=null;
     }
     
     /**
      * the name of the filter criteria sent in the request must start with "filter_"
      * @param request 
      * @return mapFilterCriteria : contains all names/values filter criteria
      */
     protected Map<String,String> getFilterCriteriaFromRequest( HttpServletRequest request ) 
     {	
    	   	
     	Enumeration enumeration = request.getParameterNames( );
     	Map<String, String> mapFilterCriteria = new HashMap<>( );
         
     	while ( enumeration.hasMoreElements( ) )
		{
			String strParameterName = (String) enumeration.nextElement();
			
			//All parameters from search bar start with the same prefix "filter_" 
			if ( strParameterName.startsWith( FILTER_ATTRIBUTES_PREFIX ) && !StringUtils.isBlank( request.getParameter( strParameterName ) ) )
			{
				mapFilterCriteria.put( strParameterName.substring(FILTER_ATTRIBUTES_PREFIX.length()), request.getParameter( strParameterName ) );
			}
		}
        return mapFilterCriteria;            	
     }  
     
     /**
      *  add persistent values of the search inputs in model
      *  @param model : map containing name parameters of http request with values associated.
      *  @param mapFilterCriteria : contains search bar names/values inputs 
      */
     protected void addSearchParameters( Map<String, Object>  model, Map<String,String> mapFilterCriteria) {
     	
    	 //Persistent values of search inputs
    	 model.put(PARAMETER_MAP_FILTER_CRITERIA, mapFilterCriteria); 
 	}

     /**
      *  get _strSortMode
      *  At each sort request, the sort mode switches (ASC to DESC and vice versa)
      */
     protected String getSortMode() {
    	 	 
    	 _strSortMode = (_strSortMode==SORT_ATTRIBUTES_ASC)?SORT_ATTRIBUTES_DESC:SORT_ATTRIBUTES_ASC;

    	 return _strSortMode;
    	 
     }
}