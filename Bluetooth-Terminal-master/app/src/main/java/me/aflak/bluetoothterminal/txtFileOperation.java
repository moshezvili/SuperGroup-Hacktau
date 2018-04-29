package me.aflak.bluetoothterminal;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.http.HttpClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.io.FileInputStream;


public class txtFileOperation {
    private  TransferUtility transferUtility;
    private Context context;
    private String userName;
    private static final String TAG = "UploadActivity";
    private static String SDPATH= Environment.getExternalStorageDirectory().getAbsolutePath();//path of the SD card


    public txtFileOperation(Context context,String userName) {
        this.context = context;
        this.userName = userName;
        File dir = new File(SDPATH + "/band");
        dir.mkdirs();
    }


    public void uploadToS3(String caseMode) {
        transferUtility = Util.getTransferUtility(this.context);
        File folder = new File(SDPATH + "/band/");
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("Upload File " + listOfFiles[i].getAbsoluteFile());
                if (listOfFiles[i].length()==0){
                    listOfFiles[i].delete();
                    continue;}
                beginUpload(listOfFiles[i]);
            }
        }
    }

    public void send(){

    }

    public void beginUpload(File file) {
        transferUtility = Util.getTransferUtility(this.context);
        final TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME,"data/data.csv",
                file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.equals(TransferState.COMPLETED)) {
                    //Success
                    Toast.makeText(context,"Success to upload"+observer.getAbsoluteFilePath(),Toast.LENGTH_SHORT).show();
//                    File del = new File(observer.getAbsoluteFilePath());
//                    del.delete();
                } else if (state.equals(TransferState.FAILED)) {
                    //Failed
                    Toast.makeText(context,"Failed to upload"+observer.getAbsoluteFilePath(),Toast.LENGTH_SHORT).show();
               }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                        id, bytesTotal, bytesCurrent));
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(TAG, "Error during upload: " + id, ex);

            }
        });

    }

    public String getSDPATH() {
        return SDPATH;
    }

    void deleteAllFiles(String ...status) {
        File folder = new File(SDPATH + "/band/");
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (status[0].equals("CANCEL")){
//                    Toast.makeText(context,"Del File " + listOfFiles[i].getAbsoluteFile(),Toast.LENGTH_SHORT).show();
                    Log.d("DELETE FILE","Del File " + listOfFiles[i].getAbsoluteFile());
                    listOfFiles[i].delete();
                }
                else if (listOfFiles[i].length()==0){
                Log.d("DELETE FILE","Del File " + listOfFiles[i].getAbsoluteFile());
                listOfFiles[i].delete();}
                else{beginUpload(listOfFiles[i]);}
            }
        }
    }
}
