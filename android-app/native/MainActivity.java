package com.yeyhome.wrongnotebook;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebChromeClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 网页上传框没有 capture 属性时，Capacitor 只会打开相册/文件选择器。
 * 这里接管 onShowFileChooser：弹出系统选择器，同时提供“拍照”和“相册/文件”。
 */
public class MainActivity extends BridgeActivity {

    private ValueCallback<Uri[]> pendingCallback;
    private WebChromeClient.FileChooserParams pendingParams;
    private Uri cameraUri;

    private ActivityResultLauncher<Intent> chooserLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        chooserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> handleResult(result.getResultCode(), result.getData())
        );
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::openChooser
        );

        super.onCreate(savedInstanceState);

        bridge.getWebView().setWebChromeClient(new BridgeWebChromeClient(bridge) {
            @Override
            public boolean onShowFileChooser(
                WebView webView,
                ValueCallback<Uri[]> callback,
                FileChooserParams params
            ) {
                return showChooser(callback, params);
            }
        });
    }

    private boolean showChooser(ValueCallback<Uri[]> callback, WebChromeClient.FileChooserParams params) {
        if (pendingCallback != null) {
            pendingCallback.onReceiveValue(null);
        }
        pendingCallback = callback;
        pendingParams = params;

        boolean hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        if (!hasCamera) {
            openChooser(false);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openChooser(true);
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
        return true;
    }

    private void openChooser(boolean withCamera) {
        if (pendingCallback == null) return;

        Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        pick.setType("image/*");
        if (pendingParams != null
                && pendingParams.getMode() == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
            pick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }

        Intent chooser = Intent.createChooser(pick, "选择图片");
        cameraUri = null;

        if (withCamera) {
            try {
                File photo = File.createTempFile("photo_", ".jpg", getCacheDir());
                cameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photo);
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                camera.setClipData(ClipData.newRawUri("photo", cameraUri));
                camera.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { camera });
            } catch (Exception e) {
                cameraUri = null;
            }
        }

        try {
            chooserLauncher.launch(chooser);
        } catch (Exception e) {
            finishWith(null);
        }
    }

    private void handleResult(int resultCode, Intent data) {
        Uri[] result = null;
        if (resultCode == RESULT_OK) {
            List<Uri> uris = new ArrayList<>();
            if (data != null && data.getClipData() != null) {
                ClipData clip = data.getClipData();
                for (int i = 0; i < clip.getItemCount(); i++) {
                    Uri u = clip.getItemAt(i).getUri();
                    if (u != null && !u.equals(cameraUri)) uris.add(u);
                }
            }
            if (uris.isEmpty() && data != null && data.getData() != null) {
                uris.add(data.getData());
            }
            if (uris.isEmpty() && cameraUri != null) {
                uris.add(cameraUri);
            }
            if (!uris.isEmpty()) result = uris.toArray(new Uri[0]);
        }
        finishWith(result);
    }

    private void finishWith(Uri[] result) {
        if (pendingCallback != null) {
            pendingCallback.onReceiveValue(result);
        }
        pendingCallback = null;
        pendingParams = null;
        cameraUri = null;
    }
}
