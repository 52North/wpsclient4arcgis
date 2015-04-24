/**
 * ﻿Copyright (C) 2013 - 2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
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
			isBase64 = Base64ConversionToolUtil.checkBase64InputFile(file, new byte[(int) file.length()]);
			
			assertTrue(isBase64);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
}
