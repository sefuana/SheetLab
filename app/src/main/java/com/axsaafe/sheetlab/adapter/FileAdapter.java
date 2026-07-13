package com.axsaafe.sheetlab.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axsaafe.sheetlab.R;
import com.axsaafe.sheetlab.model.SheetFile;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.VH> {

    public interface OnFileAction {
        void onOpen(SheetFile sf);
        void onDownload(SheetFile sf);
        void onDelete(SheetFile sf);
        void onEdit(SheetFile sf);
    }

    private final List<SheetFile> files;
    private final OnFileAction listener;
    private final Context ctx;

    public FileAdapter(Context ctx, List<SheetFile> files, OnFileAction listener) {
        this.ctx = ctx;
        this.files = files;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_file, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        SheetFile sf = files.get(pos);
        h.tvName.setText(sf.getDisplayName());

        // Highlight animation
        if (sf.isHighlighted()) {
            h.itemView.setBackgroundResource(R.drawable.bg_item_highlighted);
            ObjectAnimator anim = ObjectAnimator.ofFloat(h.itemView, "alpha", 0.4f, 1f);
            anim.setDuration(600);
            anim.setRepeatCount(3);
            anim.setRepeatMode(ObjectAnimator.REVERSE);
            anim.start();
            sf.setHighlighted(false);
        } else {
            h.itemView.setBackgroundResource(R.drawable.bg_item_normal);
        }

        h.itemView.setOnClickListener(v -> listener.onOpen(sf));

        h.ivMenu.setOnClickListener(v -> {
            // Show popup menu
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(ctx, h.ivMenu);
            popup.inflate(R.menu.menu_file_options);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_download) {
                    listener.onDownload(sf);
                    return true;
                } else if (id == R.id.action_delete) {
                    listener.onDelete(sf);
                    return true;
                } else if (id == R.id.action_edit) {
                    listener.onEdit(sf);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return files.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivMenu;

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_file_name);
            ivMenu = v.findViewById(R.id.iv_file_menu);
        }
    }
}
