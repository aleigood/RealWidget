package com.realwidget.widget.music;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.db.Button;
import com.realwidget.util.Utils;
import com.realwidget.widget.WidgetGenerator;

import java.util.ArrayList;
import java.util.List;

public class MusicWidget extends WidgetGenerator implements OnCompletionListener {
    private static MusicWidget instance;
    String[] PROJECTION = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME};
    private Context mContext;
    private List<MusicItem> playingList;
    private MusicItem current;
    private MediaPlayer mediaPlayer;

    private MusicWidget(Context context) {
        mContext = context;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        updatePlaylist();

        if (playingList.size() != 0) {
            current = playingList.get(0);
        }
    }

    public static MusicWidget getInstance(Context context) {
        if (instance == null) {
            instance = new MusicWidget(context);
        }

        return instance;
    }

    @Override
    public RemoteViews buildRemoteViews(Button[] button) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_music);
        views.setImageViewBitmap(R.id.bg, getBackImg(mContext, button[0]));

        if (current != null) {
            views.setImageViewBitmap(R.id.albumart, MusicUtils.getArtwork(mContext, current.id, current.albumId));
        }

        views.setImageViewBitmap(R.id.play,
                mediaPlayer.isPlaying() ? getIconFromRes(mContext, button[0], R.drawable.ic_music_pause)
                        : getIconFromRes(mContext, button[0], R.drawable.ic_play));
        views.setImageViewBitmap(R.id.pre, getIconFromRes(mContext, button[0], R.drawable.ic_music_pre));
        views.setImageViewBitmap(R.id.next, getIconFromRes(mContext, button[0], R.drawable.ic_music_next));
        views.setTextViewText(R.id.label, button[0].label);
        views.setTextColor(R.id.label, button[0].labelColor);

        if (current != null) {
            views.setTextViewText(R.id.title, current.title);
            views.setTextColor(R.id.title, button[0].labelColor);
            views.setTextViewText(R.id.content, current.artist);
            views.setTextColor(R.id.content, button[0].labelColor);
        }
        views.setOnClickFillInIntent(
                R.id.pre,
                new Intent(Constants.ACTION_BUTTON_CLICK)
                        .putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE, button[0].type).putExtra("cmd", 0)
                        .putExtra("button", button[0]));
        views.setOnClickFillInIntent(
                R.id.play,
                new Intent(Constants.ACTION_BUTTON_CLICK)
                        .putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE, button[0].type).putExtra("cmd", 1)
                        .putExtra("button", button[0]));
        views.setOnClickFillInIntent(
                R.id.next,
                new Intent(Constants.ACTION_BUTTON_CLICK)
                        .putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE, button[0].type).putExtra("cmd", 2)
                        .putExtra("button", button[0]));
        return views;
    }

    @Override
    public View buildViews(ViewGroup parent, Button[] button) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_widget_music, parent, false);
        view.findViewById(R.id.pos1).setTag(button[0]);
        ((ImageView) view.findViewById(R.id.bg)).setImageBitmap(getBackImg(mContext, button[0]));
        TextView label = ((TextView) view.findViewById(R.id.label));
        label.setText(button[0].label);
        label.setTextColor(button[0].labelColor);

        ((ImageView) view.findViewById(R.id.play)).setImageBitmap(getIconFromRes(mContext, button[0],
                R.drawable.ic_play));
        ((ImageView) view.findViewById(R.id.pre)).setImageBitmap(getIconFromRes(mContext, button[0],
                R.drawable.ic_music_pre));
        ((ImageView) view.findViewById(R.id.next)).setImageBitmap(getIconFromRes(mContext, button[0],
                R.drawable.ic_music_next));
        return view;
    }

    public void performAction(int cmd) {
        if (current == null) {
            return;
        }

        switch (cmd) {
            case 0:
                int preIndex = playingList.indexOf(current);

                if (preIndex - 1 <= 0) {
                    current = playingList.get(playingList.size() - 1);
                } else {
                    current = playingList.get(preIndex - 1);
                }

                play(current.id);
                break;
            case 1:

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                } else {
                    play(current.id);
                }
                break;
            case 2:
                int nextIndex = playingList.indexOf(current);

                if (nextIndex + 1 < playingList.size()) {
                    current = playingList.get(nextIndex + 1);
                } else {
                    current = playingList.get(0);
                }

                play(current.id);
                break;

            default:
                break;
        }

        Utils.updateWidgets(MusicWidget.class.getName(), mContext);
    }

    private void play(int id) {
        try {
            Uri uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(mContext, uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (current == null) {
            return;
        }

        int nextIndex = playingList.indexOf(current);

        if (nextIndex + 1 < playingList.size()) {
            current = playingList.get(nextIndex + 1);
        } else {
            current = playingList.get(0);
        }

        play(current.id);
        Utils.updateWidgets(MusicWidget.class.getName(), mContext);
    }

    public void updatePlaylist() {
        Cursor cursor = mContext.getContentResolver()
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, MediaStore.Audio.Media.IS_MUSIC + "=?",
                        new String[]{"1"}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        playingList = new ArrayList<MusicItem>();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    MusicItem item = new MusicItem();
                    item.id = cursor.getInt(0);
                    item.title = cursor.getString(1);
                    item.artist = cursor.getString(2);
                    item.albumId = cursor.getInt(3);
                    playingList.add(item);
                }
                while (cursor.moveToNext());
            }

            cursor.close();
        }
    }

    public static class MusicItem {
        int id;
        int albumId;
        String artist;
        String title;
    }
}
