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
package org.n52.client.arcmap.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class downloads a file from a URL. Based on
 * http://stackoverflow.com/questions/14069848/download-a-file-while-also-
 * updating-a-jprogressbar
 *
 * @author bpr
 *
 */
public class Download extends Observable {

    private static Logger LOGGER = LoggerFactory.getLogger(Download.class);

    // Max size of download buffer.
    private static final int MAX_BUFFER_SIZE = 1024;

    // These are the status codes.
    public static final int DOWNLOADING = 0;

    public static final int COMPLETE = 2;

    public static final int CANCELLED = 3;

    public static final int ERROR = 4;

    private URL url; // download URL

    private int size; // size of download in bytes

    private int downloaded; // number of bytes downloaded

    private int status; // current status of download

    private String resultFileName; // name of the downloaded file

    /**
     * Constructor. A tempfile will be created to store the download.
     *
     * @param url The URL to download from.
     */
    public Download(URL url, File resultFile) {
        this.url = url;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;

        if (!resultFile.exists()) {
            resultFile.getParentFile().mkdirs();
        }

        resultFileName = resultFile.getAbsolutePath();
    }

    /**
     * Constructor. A tempfile will be created to store the download.
     *
     * @param url The URL to download from.
     * @throws IOException If the tempfile could not be created.
     */
    public Download(URL url) throws IOException {
        this(url, File.createTempFile("wps-result", "tmp"));
    }

    // Get this download's URL.
    public String getUrl() {
        return url.toString();
    }

    // Get this download's size.
    public int getSize() {
        return size;
    }

    // Get this download's progress.
    public float getProgress() {
        return ((float) downloaded / size) * 100;
    }

    // Get downloaded bytes.
    public int getDownloaded() {
        return downloaded;
    }

    // Get this download's status.
    public int getStatus() {
        return status;
    }

    // Cancel this download.
    public void cancel() {
        status = CANCELLED;
        LOGGER.info("Download cancelled.");
        stateChanged();
    }

    // Mark this download as having an error.
    private void error(String message) {
        LOGGER.error(message);
        status = ERROR;
        stateChanged();
    }

    // Start or resume downloading.
    public void startDownload() {

        RandomAccessFile file = null;
        InputStream stream = null;

        try {
            // Open connection to URL.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Specify what portion of file to download.
            connection.setRequestProperty("Range", "bytes=" + downloaded + "-");

            // Connect to server.
            connection.connect();

            int responseCode = connection.getResponseCode();

            // Make sure response code is in the 200 range.
            if (responseCode / 100 != 2) {
                error(String.format("Got response code: %d, abort download.", responseCode));
            }

            // Check for valid content length.
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                error("No content-length header provided, cannot continue to download.");
            }

            /*
             * Set the size for this download if it hasn't been already set.
             */
            if (size == -1) {
                size = contentLength;
                stateChanged();
            }

            // Open file and seek to the end of it.
            file = new RandomAccessFile(resultFileName, "rw");
            file.seek(downloaded);

            stream = connection.getInputStream();

            float oldPercentage = 0;

            while (status == DOWNLOADING) {

                /*
                 * Size buffer according to how much of the file is left to
                 * download.
                 */
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }

                // Read from server into buffer.
                int read = stream.read(buffer);
                if (read == -1){
                    break;
                }

                // Write buffer to file.
                file.write(buffer, 0, read);

                downloaded += read;

                if((getProgress() - oldPercentage) >= 1){
                    stateChanged();
                    oldPercentage = getProgress();
                }
            }

            /*
             * Change status to complete if this point was reached because
             * downloading has finished.
             */
            if (status == DOWNLOADING) {
                status = COMPLETE;
                stateChanged();
            }
        } catch (Exception e) {
            error(e.getMessage());
        } finally {
            // Close file.
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {
                }
            }

            // Close connection to server.
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                }
            }
        }
    }

    // Notify observers that this download's status has changed.
    private void stateChanged() {
        setChanged();
        notifyObservers();
    }
}
