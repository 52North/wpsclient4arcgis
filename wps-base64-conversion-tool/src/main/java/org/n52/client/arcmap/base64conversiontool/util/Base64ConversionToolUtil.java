/*
 * ﻿Copyright (C) 2013 - 2016 52°North Initiative for Geospatial Open Source
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

/**
 * Utility class for the <code>Base64ConversionTool</code>.
 *
 * @author Benjamin Pross
 *
 */
public class Base64ConversionToolUtil {


    /**
     * This method adds a .base64 file extension, if the output file should be encoded in base64.
     *
     * @param outputShouldBeBase64 True if the output should be encoded in base64. The output file will get an additional .base64 extension.
     * @param outputFilePathFromGPValue The output file path specified by the <code>Base64ConversionTool</code> GP tool.
     * @return The new file path.
     */
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

    /**
     * Checks if the content of a file is encoded in base64.
     *
     * @param inputFile The file which content should be checked.
     * @return True if the content of a file is encoded in base64.
     * @throws Exception If the file could not be read.
     */
    public static boolean checkBase64InputFile(File inputFile) throws Exception {

        InputStream in = new FileInputStream(inputFile);

        byte[] inputFileAsByteArray = new byte[(int) inputFile.length()];

        in.read(inputFileAsByteArray);

        in.close();

        return Base64.isBase64(inputFileAsByteArray);
    }

}
