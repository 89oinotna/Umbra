package com.oinotna.umbra.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.oinotna.umbra.R;

import java.io.IOException;
import java.util.Objects;

public class PasswordDialog extends DialogFragment implements SurfaceHolder.Callback, ActivityResultCallback<Boolean>, Detector.Processor<Barcode>, DialogInterface.OnClickListener {

    private PasswordDialogViewModel passwordDialogViewModel;

    private SurfaceView surfaceView;
    private CameraSource cameraSource;

    private TextView txtBarcode;

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        passwordDialogViewModel = new ViewModelProvider(requireActivity()).get(PasswordDialogViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Pass null as the parent view because its going in the dialog layout
        View root = inflater.inflate(R.layout.dialog_password, null);

        txtBarcode = root.findViewById(R.id.txt_barcode);
        surfaceView = root.findViewById(R.id.surface_view);

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        barcodeDetector.setProcessor(this);

        cameraSource = new CameraSource
                .Builder(requireContext(), barcodeDetector)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(this);

        //For camera permission
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), this);

        //todo tolgo positive nutton e metto che si connette direttamente
        builder.setView(root)
                .setPositiveButton(R.string.button_ok, this)
                .setNegativeButton(R.string.button_cancel, this);

        return builder.create();
    }

    /* ------ DIALOG BUTTON -------- */
    @Override
    public void onClick(DialogInterface dialog, int button) {
        if (button == AlertDialog.BUTTON_POSITIVE) {
            passwordDialogViewModel.setPassword(txtBarcode.getText().toString());
        } else if (button == AlertDialog.BUTTON_NEGATIVE) {
            Objects.requireNonNull(this.getDialog()).dismiss();
        }
    }

    /* ------ SURFACE VIEW -------- */

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            try {
                cameraSource.start(surfaceView.getHolder());
            } catch (IOException ie) {
                ie.printStackTrace();
                Log.d("CAMERA SOURCE", Objects.requireNonNull(ie.getMessage()));
            }
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cameraSource.stop();
    }

    /* ------ BARCODE PROCESSOR -------- */

    @Override
    public void release() {

    }

    //Non runna su UI Thread
    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {
        final SparseArray<Barcode> barcodes = detections.getDetectedItems();

        if (barcodes.size() != 0) {
            // Use the post method of the TextView
            txtBarcode.post(() -> txtBarcode.setText(    // Update the TextView
                    barcodes.valueAt(0).displayValue
            ));
        }
    }

    /* ------ CALLBACK PERMISSION RESULT -------- */

    @SuppressLint("MissingPermission")
    @Override
    public void onActivityResult(Boolean isGranted) {
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
            try {
                cameraSource.start(surfaceView.getHolder());
            } catch (IOException ie) {
                ie.printStackTrace();
                Log.d("CAMERA SOURCE", ie.getMessage());
            }
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
            this.dismiss();
        }
    }
}
