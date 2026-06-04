package fr.paris.lutece.plugins.examplemuz.rs;

import fr.paris.lutece.portal.service.cache.AbstractCacheableService;

public class ProjectCacheService extends AbstractCacheableService 
{
    private static final String SERVICE_NAME = "examplemuz.ProjectCacheService";
    private static ProjectCacheService _singleton;

    private ProjectCacheService( )
    {
        initCache( SERVICE_NAME );
    }

    public static ProjectCacheService getInstance( )
    {
        if ( _singleton == null )
        {
            _singleton = new ProjectCacheService( );
        }
        return _singleton;
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}
