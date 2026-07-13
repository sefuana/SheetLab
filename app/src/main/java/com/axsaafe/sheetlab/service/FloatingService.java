package com.axsaafe.sheetlab.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.axsaafe.sheetlab.R;
import com.axsaafe.sheetlab.util.XlsxWriter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FloatingService extends Service {

    public static final String EXTRA_FILE_PATH = "file_path";
    public static final String ACTION_STOP = "action_stop";
    private static final String CHANNEL_ID = "sheetlab_float";

    private WindowManager wm;
    private View floatRoot;
    private boolean abcVisible = false;

    private String filePath;
    private int[] colNextRow = new int[3]; // next empty row for col A(0), B(1), C(2)

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        filePath = intent != null ? intent.getStringExtra(EXTRA_FILE_PATH) : null;

        // Start foreground with notification
        Intent stopIntent = new Intent(this, FloatingService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SheetLab - Easy To Fetch")
                .setContentText("Floating button active. Tap Stop to remove.")
                .setSmallIcon(R.drawable.ic_float_notif)
                .addAction(R.drawable.ic_close, "Stop", stopPending)
                .setOngoing(true)
                .build();

        startForeground(1, notif);

        setupFloatingView();
        initColNextRow();
        return START_STICKY;
    }

    private void initColNextRow() {
        colNextRow[0] = 0;
        colNextRow[1] = 0;
        colNextRow[2] = 0;
        if (filePath == null) return;
        try {
            File f = new File(filePath);
            if (!f.exists()) return;
            Workbook wb = new XSSFWorkbook(new FileInputStream(f));
            Sheet sheet = wb.getSheetAt(0);
            for (int c = 0; c < 3; c++) {
                int nextRow = 0;
                for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row != null && row.getCell(c) != null &&
                            !row.getCell(c).getStringCellValue().isEmpty()) {
                        nextRow = r + 1;
                    }
                }
                colNextRow[c] = nextRow;
            }
            wb.close();
        } catch (Exception ignored) {}
    }

    private void setupFloatingView() {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatRoot = LayoutInflater.from(this).inflate(R.layout.layout_floating, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.x = 32;
        params.y = 200;

        wm.addView(floatRoot, params);

        View btnPlus = floatRoot.findViewById(R.id.btn_float_plus);
        View btnA = floatRoot.findViewById(R.id.btn_col_a);
        View btnB = floatRoot.findViewById(R.id.btn_col_b);
        View btnC = floatRoot.findViewById(R.id.btn_col_c);

        btnA.setVisibility(View.GONE);
        btnB.setVisibility(View.GONE);
        btnC.setVisibility(View.GONE);

        // Toggle A/B/C on + click
        btnPlus.setOnClickListener(v -> {
            abcVisible = !abcVisible;
            int vis = abcVisible ? View.VISIBLE : View.GONE;
            btnA.setVisibility(vis);
            btnB.setVisibility(vis);
            btnC.setVisibility(vis);
        });

        btnA.setOnClickListener(v -> pasteToColumn(0, "A"));
        btnB.setOnClickListener(v -> pasteToColumn(1, "B"));
        btnC.setOnClickListener(v -> pasteToColumn(2, "C"));

        // Drag the + button
        final int[] lastX = {0};
        final int[] lastY = {0};
        btnPlus.setOnTouchListener((v, ev) -> {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX[0] = (int) ev.getRawX();
                    lastY[0] = (int) ev.getRawY();
                    return false; // pass to click also
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) ev.getRawX() - lastX[0];
                    int dy = (int) ev.getRawY() - lastY[0];
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                        params.x -= dx;
                        params.y -= dy;
                        lastX[0] = (int) ev.getRawX();
                        lastY[0] = (int) ev.getRawY();
                        wm.updateViewLayout(floatRoot, params);
                        return true;
                    }
                    return false;
            }
            return false;
        });
    }

    private void pasteToColumn(int colIndex, String colName) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null || !cm.hasPrimaryClip() || cm.getPrimaryClip() == null) {
            Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = cm.getPrimaryClip().getItemAt(0).getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Nothing to paste", Toast.LENGTH_SHORT).show();
            return;
        }

        if (filePath == null) {
            Toast.makeText(this, "No file linked", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File f = new File(filePath);
            Workbook wb;
            if (f.exists() && f.length() > 0) {
                wb = new XSSFWorkbook(new FileInputStream(f));
            } else {
                wb = new XSSFWorkbook();
                wb.createSheet("Sheet1");
            }

            Sheet sheet = wb.getSheetAt(0);
            int rowIdx = colNextRow[colIndex];
            Row row = sheet.getRow(rowIdx);
            if (row == null) row = sheet.createRow(rowIdx);
            Cell cell = row.createCell(colIndex);
            cell.setCellValue(text);
            colNextRow[colIndex]++;

            FileOutputStream fos = new FileOutputStream(f);
            wb.write(fos);
            fos.close();
            wb.close();

            Toast.makeText(this, "Pasted to " + colName + (rowIdx + 1), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatRoot != null && wm != null) {
            wm.removeView(floatRoot);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Easy To Fetch", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("SheetLab floating clipboard button");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }
}
