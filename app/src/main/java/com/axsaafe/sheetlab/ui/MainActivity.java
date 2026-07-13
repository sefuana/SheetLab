package com.axsaafe.sheetlab.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axsaafe.sheetlab.R;
import com.axsaafe.sheetlab.SheetLabApp;
import com.axsaafe.sheetlab.adapter.FileAdapter;
import com.axsaafe.sheetlab.model.SheetFile;
import com.axsaafe.sheetlab.util.FileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FileAdapter.OnFileAction {

    public static final String EXTRA_HIGHLIGHT_PATH = "highlight_path";

    private FileManager fileManager;
    private FileAdapter adapter;
    private List<SheetFile> currentList = new ArrayList<>();
    private EditText etSearch;
    private String highlightPath;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(SheetLabApp.PREF_NAME, MODE_PRIVATE);
        fileManager = new FileManager(this);

        // Dark/Light mode toggle (top-left)
        ImageView ivTheme = findViewById(R.id.iv_theme_toggle);
        updateThemeIcon(ivTheme);
        ivTheme.setOnClickListener(v -> toggleTheme(ivTheme));

        // Search bar
        etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                filterFiles(s.toString());
            }
            @Override public void afterTextChanged(Editable e) {}
        });

        // RecyclerView
        RecyclerView rv = findViewById(R.id.rv_files);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FileAdapter(this, currentList, this);
        rv.setAdapter(adapter);

        // FAB - new spreadsheet (bottom right)
        View fab = findViewById(R.id.fab_new);
        fab.setOnClickListener(v -> showNewFileDialog());

        // Highlight path from intent (back from editor)
        highlightPath = getIntent().getStringExtra(EXTRA_HIGHLIGHT_PATH);

        loadFiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFiles();
    }

    private void loadFiles() {
        String query = etSearch.getText().toString();
        if (query.isEmpty()) {
            currentList.clear();
            currentList.addAll(fileManager.getAllFiles());
        } else {
            currentList.clear();
            currentList.addAll(fileManager.searchFiles(query));
        }

        // Apply highlight
        if (highlightPath != null) {
            for (SheetFile sf : currentList) {
                if (sf.getPath().equals(highlightPath)) {
                    sf.setHighlighted(true);
                    break;
                }
            }
            highlightPath = null;
        }

        adapter.notifyDataSetChanged();
    }

    private void filterFiles(String query) {
        currentList.clear();
        if (query.isEmpty()) {
            currentList.addAll(fileManager.getAllFiles());
        } else {
            currentList.addAll(fileManager.searchFiles(query));
        }
        adapter.notifyDataSetChanged();
    }

    private void showNewFileDialog() {
        EditText et = new EditText(this);
        et.setHint("File name");
        new AlertDialog.Builder(this)
                .setTitle("New Spreadsheet")
                .setView(et)
                .setPositiveButton("Create", (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (!name.isEmpty()) {
                        try {
                            SheetFile sf = fileManager.createNewFile(name);
                            Intent intent = new Intent(this, EditorActivity.class);
                            intent.putExtra(EditorActivity.EXTRA_FILE_PATH, sf.getPath());
                            intent.putExtra(EditorActivity.EXTRA_FILE_NAME, sf.getName());
                            startActivity(intent);
                        } catch (IOException e) {
                            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleTheme(ImageView iv) {
        boolean isDark = prefs.getBoolean(SheetLabApp.KEY_DARK_MODE, true);
        isDark = !isDark;
        prefs.edit().putBoolean(SheetLabApp.KEY_DARK_MODE, isDark).apply();
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        updateThemeIcon(iv);
    }

    private void updateThemeIcon(ImageView iv) {
        boolean isDark = prefs.getBoolean(SheetLabApp.KEY_DARK_MODE, true);
        iv.setImageResource(isDark ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
    }

    @Override
    public void onOpen(SheetFile sf) {
        Intent intent = new Intent(this, EditorActivity.class);
        intent.putExtra(EditorActivity.EXTRA_FILE_PATH, sf.getPath());
        intent.putExtra(EditorActivity.EXTRA_FILE_NAME, sf.getName());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onEdit(SheetFile sf) {
        onOpen(sf);
    }

    @Override
    public void onDownload(SheetFile sf) {
        try {
            File src = sf.toFile();
            File dst = fileManager.getDownloadTarget(sf);
            copyFile(src, dst);
            Toast.makeText(this, "Downloaded to Downloads/" + sf.getName(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDelete(SheetFile sf) {
        new AlertDialog.Builder(this)
                .setTitle("Delete File")
                .setMessage("Delete \"" + sf.getDisplayName() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    if (fileManager.deleteFile(sf)) {
                        loadFiles();
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void copyFile(File src, File dst) throws IOException {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst);
             FileChannel inCh = in.getChannel();
             FileChannel outCh = out.getChannel()) {
            inCh.transferTo(0, inCh.size(), outCh);
        }
    }
}
