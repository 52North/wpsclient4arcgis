/*
 * ﻿Copyright (C) 2013 - 2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.client.arcmap.base64conversiontool.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;
import org.n52.client.arcmap.base64conversiontool.util.Base64ConversionToolUtil;

public class Base64ConversionToolTest {


	@Test
	public void testGetNewOutputFileNameAddBase64(){

		String path = "./elev_ned_30m1_cropped.tif";

		String newPath = Base64ConversionToolUtil.getNewOutputFileNameIfApplicable(true, path);

		System.out.println(newPath);

		assertTrue(newPath.contains(".base64.tif"));

	}

	@Test
	public void testGetNewOutputFileNameRemoveBase64(){

		String path = "./mockupData.base64.tif";

		String newPath = Base64ConversionToolUtil.getNewOutputFileNameIfApplicable(false, path);

		System.out.println(newPath);

		assertTrue(!newPath.contains(".base64"));

	}

	@Test
	public void testCheckBase64(){

		String path = "src/test/resources/mockupData.base64.tif";

		File file = new File(path);

		boolean isBase64;
		try {
			isBase64 = Base64ConversionToolUtil.checkBase64InputFile(file);

			assertTrue(isBase64);
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}

}
