package me.aflak.bluetoothterminal;

/**
 * Created by uri on 18/01/2018.
 */

public class Constants {

    /*
     * You should replace these values with your own. See the README for details
     * on what to fill in.
     */
    public static final String COGNITO_POOL_ID = "eu-central-1:775434bd-d6ea-404e-b626-4a0a1f0c575b";

    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "eu-central-1";

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String BUCKET_NAME = "alcotest";

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "eu-central-1";
}