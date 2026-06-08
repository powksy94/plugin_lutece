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
package fr.paris.lutece.plugins.examplemuz.business;

import fr.paris.lutece.test.LuteceTestCase;

import java.util.Optional;

/**
 * This is the business class test for the object Project
 */
public class ProjectBusinessTest extends LuteceTestCase
{
    private static final String NAME1 = "Name1";
    private static final String NAME2 = "Name2";
    private static final String DESCRIPTION1 = "Description1";
    private static final String DESCRIPTION2 = "Description2";
    private static final String IMAGEURL1 = "ImageUrl1";
    private static final String IMAGEURL2 = "ImageUrl2";

    /**
     * test Project
     */
    public void testBusiness( )
    {
        // Initialize an object
        Project project = new Project( );
        project.setName( NAME1 );
        project.setDescription( DESCRIPTION1 );
        project.setImageUrl( IMAGEURL1 );

        // Create test
        ProjectHome.create( project );
        Optional<Project> optProjectStored = ProjectHome.findByPrimaryKey( project.getId( ) );
        Project projectStored = optProjectStored.orElse( new Project( ) );
        assertEquals( projectStored.getName( ), project.getName( ) );
        assertEquals( projectStored.getDescription( ), project.getDescription( ) );
        assertEquals( projectStored.getImageUrl( ), project.getImageUrl( ) );

        // Update test
        project.setName( NAME2 );
        project.setDescription( DESCRIPTION2 );
        project.setImageUrl( IMAGEURL2 );
        ProjectHome.update( project );
        optProjectStored = ProjectHome.findByPrimaryKey( project.getId( ) );
        projectStored = optProjectStored.orElse( new Project( ) );

        assertEquals( projectStored.getName( ), project.getName( ) );
        assertEquals( projectStored.getDescription( ), project.getDescription( ) );
        assertEquals( projectStored.getImageUrl( ), project.getImageUrl( ) );

        // List test
        ProjectHome.getProjectsList( );

        // Delete test
        ProjectHome.remove( project.getId( ) );
        optProjectStored = ProjectHome.findByPrimaryKey( project.getId( ) );
        projectStored = optProjectStored.orElse( null );
        assertNull( projectStored );

    }

}
