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
package org.n52.client.arcmap.base64conversiontool.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;

public class Base64ConversionToolUtil {

    public static String getNewOutputFileNameIfApplicable(boolean outputShouldBeBase64,
            String outputFilePathFromGPValue) {
        /*
         * check if output should be base64 and in case add .base64 before
         * extension
         */
        String extension = outputFilePathFromGPValue.substring(outputFilePathFromGPValue.lastIndexOf("."));

        String newOutputFilePath = outputFilePathFromGPValue;

        if (outputShouldBeBase64 && !outputFilePathFromGPValue.contains(".base64")) {
            extension = "base64" + extension;
            newOutputFilePath = outputFilePathFromGPValue.substring(0, outputFilePathFromGPValue.lastIndexOf(".") + 1) + extension;

        } else if (!outputShouldBeBase64 && outputFilePathFromGPValue.contains(".base64")) {
            newOutputFilePath = outputFilePathFromGPValue.replace(".base64", "");
        }

        return newOutputFilePath;
    }

    public static boolean checkBase64InputFile(File inputFile,
            byte[] inputFileAsByteArray) throws Exception {

        InputStream in = new FileInputStream(inputFile);

        inputFileAsByteArray = new byte[(int) inputFile.length()];

        in.read(inputFileAsByteArray);

        in.close();

        return Base64.isBase64(inputFileAsByteArray);
    }

}
