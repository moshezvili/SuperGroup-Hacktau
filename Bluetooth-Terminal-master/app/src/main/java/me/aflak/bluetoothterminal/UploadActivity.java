package me.aflak.bluetoothterminal;

import android.os.Bundle;
import android.app.ListActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;


/**
 * UploadActivity is a ListActivity of uploading, and uploaded records as well
 * as buttons for managing the uploads and creating new ones.
 */
public class UploadActivity extends ListActivity {

    // Indicates that no upload is currently selected
    private static final int INDEX_NOT_CHECKED = -1;

    // TAG for logging;
    private static final String TAG = "UploadActivity";

    // Button for upload operations
    private Button btnPause;
    private Button btnResume;
    private Button btnCancel;
    private Button btnDelete;
    private Button btnPauseAll;
    private Button btnCancelAll;

    // The TransferUtility is the primary class for managing transfer to S3
    private TransferUtility transferUtility;

    // The SimpleAdapter adapts the data about transfers to rows in the UI
    private SimpleAdapter simpleAdapter;

    // A List of all transfers
    private List<TransferObserver> observers;

    /**
     * This map is used to provide data to the SimpleAdapter above. See the
     * fillMap() function for how it relates observers to rows in the displayed
     * activity.
     */
    private ArrayList<HashMap<String, Object>> transferRecordMaps;

    // Which row in the UI is currently checked (if any)
    private int checkedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initializes TransferUtility, always do this before using it.
        transferUtility = Util.getTransferUtility(this);
        checkedIndex = INDEX_NOT_CHECKED;
        transferRecordMaps = new ArrayList<HashMap<String, Object>>();
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get the data from any transfer's that have already happened,
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clear transfer listeners to prevent memory leak, or
        // else this activity won't be garbage collected.
        if (observers != null && !observers.isEmpty()) {
            for (TransferObserver observer : observers) {
                observer.cleanTransferListener();
            }
        }
    }

    /**
     * Gets all relevant transfers from the Transfer Service for populating the
     * UI
     */
    private void initData() {
        transferRecordMaps.clear();
        // Use TransferUtility to get all upload transfers.
        observers = transferUtility.getTransfersWithType(TransferType.UPLOAD);
        TransferListener listener = new UploadListener();
        for (TransferObserver observer : observers) {

            // For each transfer we will will create an entry in
            // transferRecordMaps which will display
            // as a single row in the UI
            HashMap<String, Object> map = new HashMap<String, Object>();
            Util.fillMap(map, observer, false);
            transferRecordMaps.add(0,map);

            // Sets listeners to in progress transfers
            if (TransferState.WAITING.equals(observer.getState())
                    || TransferState.WAITING_FOR_NETWORK.equals(observer.getState())
                    || TransferState.IN_PROGRESS.equals(observer.getState())) {
                observer.setTransferListener(listener);
            }
        }
        simpleAdapter.notifyDataSetChanged();
    }

    private void initUI() {
        /**
         * This adapter takes the data in transferRecordMaps and displays it,
         * with the keys of the map being related to the columns in the adapter
         */
        simpleAdapter = new SimpleAdapter(this, transferRecordMaps,
                R.layout.record_item, new String[] {
                "checked", "fileName", "progress", "bytes", "state", "percentage"
        },
                new int[] {
                        R.id.radioButton1, R.id.textFileName, R.id.progressBar1, R.id.textBytes,
                        R.id.textState, R.id.textPercentage
                });
        simpleAdapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                switch (view.getId()) {
                    case R.id.radioButton1:
                        RadioButton radio = (RadioButton) view;
                        radio.setChecked((Boolean) data);
                        return true;
                    case R.id.textFileName:
                        TextView fileName = (TextView) view;
                        fileName.setText((String) data);
                        return true;
                    case R.id.progressBar1:
                        ProgressBar progress = (ProgressBar) view;
                        progress.setProgress((Integer) data);
                        return true;
                    case R.id.textBytes:
                        TextView bytes = (TextView) view;
                        bytes.setText((String) data);
                        return true;
                    case R.id.textState:
                        TextView state = (TextView) view;
                        state.setText(((TransferState) data).toString());
                        return true;
                    case R.id.textPercentage:
                        TextView percentage = (TextView) view;
                        percentage.setText((String) data);
                        return true;
                }
                return false;
            }
        });
        setListAdapter(simpleAdapter);

        // Updates checked index when an item is clicked
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {

                if (checkedIndex != pos) {
                    transferRecordMaps.get(pos).put("checked", true);
                    if (checkedIndex >= 0) {
                        transferRecordMaps.get(checkedIndex).put("checked", false);
                    }
                    checkedIndex = pos;
                    updateButtonAvailability();
                    simpleAdapter.notifyDataSetChanged();
                }
            }
        });

        btnPause = (Button) findViewById(R.id.buttonPause);
        btnResume = (Button) findViewById(R.id.buttonResume);
        btnCancel = (Button) findViewById(R.id.buttonCancel);
        btnDelete = (Button) findViewById(R.id.buttonDelete);
        btnPauseAll = (Button) findViewById(R.id.buttonPauseAll);
        btnCancelAll = (Button) findViewById(R.id.buttonCancelAll);

        btnPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make sure the user has selected a transfer
                if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                    Boolean paused = transferUtility.pause(observers.get(checkedIndex).getId());
                    /**
                     * If paused does not return true, it is likely because the
                     * user is trying to pause an upload that is not in a
                     * pausable state (For instance it is already paused, or
                     * canceled).
                     */
                    if (!paused) {
                        Toast.makeText(
                                UploadActivity.this,
                                "Cannot pause transfer.  You can only pause transfers in a IN_PROGRESS or WAITING state.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnResume.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make sure the user has selected a transfer
                if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                    TransferObserver resumed = transferUtility.resume(observers.get(checkedIndex)
                            .getId());
                    // Sets a new transfer listener to the original observer.
                    // This will overwrite existing listener.
                    observers.get(checkedIndex).setTransferListener(new UploadListener());
                    /**
                     * If resume returns null, it is likely because the transfer
                     * is not in a resumable state (For instance it is already
                     * running).
                     */
                    if (resumed == null) {
                        Toast.makeText(
                                UploadActivity.this,
                                "Cannot resume transfer.  You can only resume transfers in a PAUSED state.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make sure a transfer is selected
                if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                    Boolean canceled = transferUtility.cancel(observers.get(checkedIndex).getId());
                    /**
                     * If cancel returns false, it is likely because the
                     * transfer is already canceled
                     */
                    if (!canceled) {
                        Toast.makeText(
                                UploadActivity.this,
                                "Cannot cancel transfer.  You can only resume transfers in a PAUSED, WAITING, or IN_PROGRESS state.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make sure a transfer is selected
                if (checkedIndex >= 0 && checkedIndex < observers.size()) {
                    transferUtility.deleteTransferRecord(observers.get(checkedIndex).getId());
                    observers.remove(checkedIndex);
                    transferRecordMaps.remove(checkedIndex);
                    checkedIndex = INDEX_NOT_CHECKED;
                    updateButtonAvailability();
                    updateList();
                }
            }
        });

        btnPauseAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < observers.size(); i++) {
                    TransferObserver observer = observers.get(i);
                    if (observer.getId() >= 0 && observer.getId() < observers.size()) {
                        transferUtility.deleteTransferRecord(observer.getId());
                        observers.remove(observer.getId());
                        transferRecordMaps.remove(observer.getId());
                    }
                }
                updateList();
            }

        });

        btnCancelAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                transferUtility.cancelAllWithType(TransferType.UPLOAD);
            }
        });

        updateButtonAvailability();
    }

    /*
     * Updates the ListView according to the observers.
     */
    private void updateList() {
        TransferObserver observer = null;
        HashMap<String, Object> map = null;
        for (int i = 0; i < observers.size(); i++) {
            observer = observers.get(i);
            map = transferRecordMaps.get(i);
            Util.fillMap(map, observer, i == checkedIndex);
        }
        simpleAdapter.notifyDataSetChanged();

    }

    /*
     * Enables or disables buttons according to checkedIndex.
     */
    private void updateButtonAvailability() {
        boolean availability = checkedIndex >= 0;
        btnPause.setEnabled(availability);
        btnResume.setEnabled(availability);
        btnCancel.setEnabled(availability);
        btnDelete.setEnabled(availability);
    }

    /*
     * A TransferListener class that can listen to a upload task and be notified
     * when the status changes.
     */
    private class UploadListener implements TransferListener {

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e(TAG, "Error during upload: " + id, e);
            updateList();
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d(TAG, String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));
            updateList();
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d(TAG, "onStateChanged: " + id + ", " + newState);
            updateList();
        }
    }
}

