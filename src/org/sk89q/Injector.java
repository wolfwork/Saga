<<<<<<< HEAD
// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
*/

package org.sk89q;

import java.lang.reflect.InvocationTargetException;

/**
 * Constructs new instances.
 */
public interface Injector {

    /**
     * Constructs a new instance of the given class.
     * 
     * @param cls class
     * @return object
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public Object getInstance(Class<?> cls) throws InvocationTargetException,
            IllegalAccessException, InstantiationException;

}
=======
// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
*/

package org.sk89q;

import java.lang.reflect.InvocationTargetException;

/**
 * Constructs new instances.
 */
public interface Injector {

    /**
     * Constructs a new instance of the given class.
     * 
     * @param cls class
     * @return object
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public Object getInstance(Class<?> cls) throws InvocationTargetException,
            IllegalAccessException, InstantiationException;

}
>>>>>>> 077be13b22fa6a73d023a156eb6cc0318a9ba1be
