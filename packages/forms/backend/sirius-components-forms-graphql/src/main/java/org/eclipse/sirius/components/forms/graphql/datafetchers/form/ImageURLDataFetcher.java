/*******************************************************************************
 * Copyright (c) 2022, 2024 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.sirius.components.forms.graphql.datafetchers.form;

import java.util.Objects;

import org.eclipse.sirius.components.annotations.spring.graphql.QueryDataFetcher;
import org.eclipse.sirius.components.core.api.IImageURLSanitizer;
import org.eclipse.sirius.components.forms.Image;
import org.eclipse.sirius.components.graphql.api.IDataFetcherWithFieldCoordinates;
import org.eclipse.sirius.components.graphql.api.URLConstants;

import graphql.schema.DataFetchingEnvironment;

/**
 * Data fetcher for Image.url, to rewrite the relative path of the image into an absolute path on the server.
 * <p>
 * If the <code>Image.url</code> is of the form <code>path/to/image.svg</code>, the rewritten value which will be seen
 * by the frontend will be <code>/api/images/path/to/image.svg</code>.
 *
 * @author pcdavid
 */
@QueryDataFetcher(type = "Image", field = "url")
public class ImageURLDataFetcher implements IDataFetcherWithFieldCoordinates<String> {

    private final IImageURLSanitizer imageURLSanitizer;

    public ImageURLDataFetcher(IImageURLSanitizer imageURLSanitizer) {
        this.imageURLSanitizer = Objects.requireNonNull(imageURLSanitizer);
    }

    @Override
    public String get(DataFetchingEnvironment environment) throws Exception {
        Image image = environment.getSource();
        return this.imageURLSanitizer.sanitize(URLConstants.IMAGE_BASE_PATH, image.getUrl());
    }
}
