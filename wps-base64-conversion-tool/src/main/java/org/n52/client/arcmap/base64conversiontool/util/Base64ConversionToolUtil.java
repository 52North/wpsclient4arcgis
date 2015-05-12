/**
 * ﻿Copyright (C) 2013 - 2015 52°North Initiative for Geospatial Open Source
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
