package com.axsaafe.sheetlab.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.axsaafe.sheetlab.R;
import com.axsaafe.sheetlab.service.FloatingService;
import com.axsaafe.sheetlab.util.FileManager;
import com.axsaafe.sheetlab.model.SheetFile;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends AppCompatActivity {

    public static final String EXTRA_FILE_PATH = "file_path";
    public static final String EXTRA_FILE_NAME = "file_name";
    private static final int OVERLAY_PERMISSION_REQ = 1001;

    private String filePath;
    private String fileName;
    private FileManager fileManager;
    private TableLayout tableLayout;
    private List<List<EditText>> cells = new ArrayList<>();
    private int numRows = 50;
    private int numCols = 26; // A-Z

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        fileManager = new FileManager(this);
        filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        fileName = getIntent().getStringExtra(EXTRA_FILE_NAME);

        // File name display (top left, 3 dots menu)
        TextView tvFileName = findViewById(R.id.tv_file_name);
        tvFileName.setText(fileName != null ? fileName.replace(".xlsx", "") : "Sheet");

        // 3 dots menu top-left
        ImageView ivOptions = findViewById(R.id.iv_sheet_options);
        ivOptions.setOnClickListener(v -> showSheetOptionsMenu(v));

        // Save button (✓ tick)
        ImageView ivSave = findViewById(R.id.iv_save);
        ivSave.setOnClickListener(v -> saveFile());

        // Back button
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> onBackPressed());

        tableLayout = findViewById(R.id.table_layout);
        loadOrCreateSheet();
    }

    private void showSheetOptionsMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Rename File");
        popup.getMenu().add(0, 2, 1, "Easy To Fetch");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                showRenameDialog();
            } else if (item.getItemId() == 2) {
                startEasyToFetch();
            }
            return true;
        });
        popup.show();
    }

    private void showRenameDialog() {
        EditText et = new EditText(this);
        String current = fileName != null ? fileName.replace(".xlsx", "") : "";
        et.setText(current);
        et.setSelectAllOnFocus(true);
        new AlertDialog.Builder(this)
                .setTitle("Rename File")
                .setMessage("Name (.xlsx will be kept)")
                .setView(et)
                .setPositiveButton("Rename", (d, w) -> {
                    String newName = et.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(current)) {
                        SheetFile sf = new SheetFile(fileName, filePath, 0);
                        if (fileManager.renameFile(sf, newName)) {
                            filePath = sf.getPath();
                            fileName = sf.getName();
                            TextView tvFileName = findViewById(R.id.tv_file_name);
                            tvFileName.setText(newName);
                            Toast.makeText(this, "Renamed to " + newName + ".xlsx", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Rename failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startEasyToFetch() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ);
        } else {
            launchFloatingService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ) {
            if (Settings.canDrawOverlays(this)) {
                launchFloatingService();
            } else {
                Toast.makeText(this, "Overlay permission needed for Easy To Fetch", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void launchFloatingService() {
        Intent svc = new Intent(this, FloatingService.class);
        svc.putExtra(FloatingService.EXTRA_FILE_PATH, filePath);
        startForegroundService(svc);
        Toast.makeText(this, "Easy To Fetch activated! Use the + button.", Toast.LENGTH_SHORT).show();
    }

    private void loadOrCreateSheet() {
        tableLayout.removeAllViews();
        cells.clear();

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
            int maxRows = Math.max(numRows, sheet.getLastRowNum() + 1);

            // Column headers (A, B, C...)
            TableRow headerRow = new TableRow(this);
            headerRow.addView(makeHeaderCell(""));
            for (int c = 0; c < numCols; c++) {
                headerRow.addView(makeHeaderCell(String.valueOf((char) ('A' + c))));
            }
            tableLayout.addView(headerRow);

            for (int r = 0; r < maxRows; r++) {
                TableRow tr = new TableRow(this);
                // Row number
                tr.addView(makeHeaderCell(String.valueOf(r + 1)));

                List<EditText> rowCells = new ArrayList<>();
                Row poiRow = sheet.getRow(r);
                for (int c = 0; c < numCols; c++) {
                    String val = "";
                    if (poiRow != null) {
                        Cell cell = poiRow.getCell(c);
                        if (cell != null) {
                            if (cell.getCellType() == CellType.STRING) val = cell.getStringCellValue();
                            else if (cell.getCellType() == CellType.NUMERIC) val = String.valueOf((long) cell.getNumericCellValue());
                            else val = "";
                        }
                    }
                    EditText et = makeCellEditText(val);
                    rowCells.add(et);
                    tr.addView(et);
                }
                cells.add(rowCells);
                tableLayout.addView(tr);
            }
            wb.close();
        } catch (Exception e) {
            Toast.makeText(this, "Error loading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private TextView makeHeaderCell(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(12, 8, 12, 8);
        tv.setGravity(Gravity.CENTER);
        tv.setTextAppearance(R.style.CellHeader);
        tv.setMinWidth(80);
        return tv;
    }

    private EditText makeCellEditText(String value) {
        EditText et = new EditText(this);
        et.setText(value);
        et.setPadding(8, 6, 8, 6);
        et.setMinWidth(120);
        et.setMaxWidth(200);
        et.setSingleLine(true);
        et.setBackground(getDrawable(R.drawable.bg_cell));
        et.setTextAppearance(R.style.CellText);
        return et;
    }

    private void saveFile() {
        try {
            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("Sheet1");

            for (int r = 0; r < cells.size(); r++) {
                Row row = sheet.createRow(r);
                List<EditText> rowCells = cells.get(r);
                for (int c = 0; c < rowCells.size(); c++) {
                    String val = rowCells.get(c).getText().toString();
                    if (!val.isEmpty()) {
                        Cell cell = row.createCell(c);
                        cell.setCellValue(val);
                    }
                }
            }

            FileOutputStream fos = new FileOutputStream(filePath);
            wb.write(fos);
            fos.close();
            wb.close();

            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Save error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Go back and highlight this file
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_HIGHLIGHT_PATH, filePath);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    // Called by FloatingService to write clipboard text to a column
    public static String currentFilePath = null;

    @Override
    protected void onStart() {
        super.onStart();
        currentFilePath = filePath;
    }

    @Override
    protected void onStop() {
        super.onStop();
        currentFilePath = null;
    }
}
