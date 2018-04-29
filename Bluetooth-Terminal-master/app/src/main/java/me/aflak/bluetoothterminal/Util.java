/*
 * Copyright 2015-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package me.aflak.bluetoothterminal;

import android.content.Context;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.util.Map;

/*
 * Handles basic helper functions used throughout the app.
 */
public class Util {

    // We only need one instance of the clients and credentials provider
    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static TransferUtility sTransferUtility;

//    /**
//     * Gets an instance of CognitoCachingCredentialsProvider which is
//     * constructed using the given Context.
//     *
//     * @param context An Context instance.
//     * @return A default credential provider.
//     */
//    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
//        if (sCredProvider == null) {
//            sCredProvider = new CognitoCachingCredentialsProvider(
//                    context.getApplicationContext(),
//                    Constants.COGNITO_POOL_ID,
//                    Regions.EU_CENTRAL_1);
//        }
//        return sCredProvider;
//    }

    /**
     * Gets an instance of a S3 client which is constructed using the given
     * Context.
     *
     * @return A default S3 client.
     */
    public static AmazonS3Client getS3Client() {
        if (sS3Client == null) {
            final AWSCredentials credentials =  new BasicAWSCredentials("AKIAI7TMKJJICTWB72ZQ", "51BNuVO5X7SFHTJBXVAuPBDyiFLnzAL6a0Z/6RRd");
            sS3Client = new AmazonS3Client(credentials);
            sS3Client.setRegion(Region.getRegion(Regions.US_WEST_2));
        }
        return sS3Client;
    }

    /**
     * Gets an instance of the TransferUtility which is constructed using the
     * given Context
     *
     * @param context
     * @return a TransferUtility instance
     */
    public static TransferUtility getTransferUtility(Context context) {
        if (sTransferUtility == null) {
            sTransferUtility = TransferUtility.builder().s3Client(getS3Client()).context(context.getApplicationContext()).build();
        }
        return sTransferUtility;
    }

    /**
     * Converts number of bytes into proper scale.
     *
     * @param bytes number of bytes to be converted.
     * @return A string that represents the bytes in a proper scale.
     */
    public static String getBytesString(long bytes) {
        String[] quantifiers = new String[] {
                "KB", "MB", "GB", "TB"
        };
        double speedNum = bytes;
        for (int i = 0;; i++) {
            if (i >= quantifiers.length) {
                return "";
            }
            speedNum /= 1024;
            if (speedNum < 512) {
                return String.format("%.2f", speedNum) + " " + quantifiers[i];
            }
        }
    }

    /*
     * Fills in the map with information in the observer so that it can be used
     * with a SimpleAdapter to populate the UI
     */
    public static void fillMap(Map<String, Object> map, TransferObserver observer, boolean isChecked) {
        int progress = (int) ((double) observer.getBytesTransferred() * 100 / observer
                .getBytesTotal());
        map.put("id", observer.getId());
        map.put("checked", isChecked);
        map.put("fileName", observer.getKey());
        map.put("progress", progress);
        map.put("bytes",
                getBytesString(observer.getBytesTransferred()) + "/"
                        + getBytesString(observer.getBytesTotal()));
        map.put("state", observer.getState());
        map.put("percentage", progress + "%");
    }

}
