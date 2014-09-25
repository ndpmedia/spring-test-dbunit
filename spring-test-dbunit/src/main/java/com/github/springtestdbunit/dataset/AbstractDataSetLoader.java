/*
 * Copyright 2002-2013 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.springtestdbunit.dataset;

import java.lang.reflect.Method;

import org.dbunit.dataset.IDataSet;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Abstract data set loader, which provides a basis for concrete implementations of the {@link DataSetLoader} strategy.
 * Provides a <em>Template Method</em> based approach for {@link #loadDataSet(Class,Method,String,String) loading} data
 * using a Spring {@link #getResourceLoader resource loader}.
 *
 * @author Phillip Webb
 *
 * @see #getResourceLoader
 * @see #getResourceLocations
 * @see #createDataSet(Resource)
 */
public abstract class AbstractDataSetLoader implements DataSetLoader {

	/**
	 * Loads a {@link IDataSet dataset} from {@link Resource}s obtained from the specified <tt>location</tt>. Each
	 * <tt>location</tt> can be mapped to a number of potential {@link #getResourceLocations resources}, the first
	 * resource that {@link Resource#exists() exists} will be used. {@link Resource}s are loaded using the
	 * {@link ResourceLoader} returned from {@link #getResourceLoader}.
	 * <p>
	 * If no resource can be found then <tt>null</tt> will be returned.
	 *
	 * @see #createDataSet(Resource)
	 * @see com.github.springtestdbunit.dataset.DataSetLoader#loadDataSet(Class,Method,String,String) java.lang.String)
	 */
	public IDataSet loadDataSet(Class<?> testClass, Method testMethod, String location, String suffix) throws Exception {
		ResourceLoader[] resourceLoaders = getResourceLoader(testClass);
		for (ResourceLoader resourceLoader : resourceLoaders) {
			String[] resourceLocations = getResourceLocations(testClass, testMethod, location, suffix);
			for (String resourceLocation : resourceLocations) {
				Resource resource = resourceLoader.getResource(resourceLocation);
				if (resource.exists()) {
					return createDataSet(resource);
				}
			}
		}
		return null;
	}

	/**
	 * Gets the {@link ResourceLoader} that will be used to load the dataset {@link Resource}s.
	 * @param testClass The class under test
	 * @return resource loader list
	 */
	protected ResourceLoader[] getResourceLoader(Class<?> testClass) {
		return new ResourceLoader[] { new ClassRelativeResourceLoader(testClass), new DefaultResourceLoader() };
	}

	/**
	 * Get the resource locations that should be considered when attempting to load a dataset from the specified
	 * location.
	 * @param testClass The class under test
	 * @param testMethod The method under test
	 * @param location The source location
	 * @param suffix The resource path suffix, e.g. 'expected.xml','setup.xml','teardown.xml'
	 * @return an array of potential resource locations
	 */
	protected String[] getResourceLocations(Class<?> testClass, Method testMethod, String location, String suffix) {
		if ((location != null) && !"".equals(location)) {
			return new String[] { location };
		}
		String[] list = new String[3];
		list[0] = testClass.getSimpleName() + "-" + testMethod.getName() + "-" + suffix;
		list[1] = testClass.getSimpleName() + "-" + suffix;
		list[2] = suffix;
		return list;
	}

	/**
	 * Factory method used to create the {@link IDataSet dataset}
	 * @param resource an existing resource that contains the dataset data
	 * @return a dataset
	 * @throws Exception if the dataset could not be loaded
	 */
	protected abstract IDataSet createDataSet(Resource resource) throws Exception;
}
