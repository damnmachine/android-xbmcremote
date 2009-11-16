/*
 *      Copyright (C) 2005-2009 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.remote.activity;

import java.io.File;
import java.util.ArrayList;

import org.xbmc.android.remote.R;
import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.backend.HttpApiHandler;
import org.xbmc.backend.HttpApiHandlerAdapter;
import org.xbmc.backend.async.thread.HttpApiThread;
import org.xbmc.backend.exceptions.BackendException;
import org.xbmc.backend.httpapi.type.ThumbSize;
import org.xbmc.model.Album;
import org.xbmc.model.Song;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Keeps all the more sophisticated dialogs in one place. Feel free to change
 * if it gets inconvenient.
 * 
 * @author Team XBMC
 */
public abstract class DialogFactory {
	
	/**
	 * Returns the album details dialog, the one with big cover and queue/play button.
	 * @param activity Parent activity
	 * @param album    Album to display
	 * @return Album details dialog
	 */
	public static Dialog getAlbumDetail(final Activity activity, final Album album) {
		
		final Dialog dialog = new Dialog(activity);
		dialog.setContentView(R.layout.albuminfo);
		
		// DEPRECATED
		try {
			ConnectionManager.getHttpClient(activity).music.updateAlbumInfo(album);
		} catch (BackendException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		dialog.setTitle(album.name);

		// get controls
		final TextView artistText = (TextView) dialog.findViewById(R.id.album_artistname);
		final ImageView cover = (ImageView) dialog.findViewById(R.id.album_cover);
		final TextView genresText = (TextView) dialog.findViewById(R.id.album_genres);
		final TextView yearText = (TextView) dialog.findViewById(R.id.album_year);
		final Button queueButton = (Button) dialog.findViewById(R.id.album_queuebutton);
		final Button playButton = (Button) dialog.findViewById(R.id.album_playbutton);
		
		// set the button's listener
		queueButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				HttpApiThread.music().play(new HttpApiHandlerAdapter<Boolean>(activity), album);
			}
		});
		playButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				HttpApiThread.music().addToPlaylist(new HttpApiHandlerAdapter<Boolean>(activity), album);
			}
		});
		
		// update content
		artistText.setText(album.artist);
		if (album.year > 0) {
			yearText.setText(String.valueOf(album.year));
		} else {
			yearText.setVisibility(View.GONE);
		}
		if (album.genres != null) {
			genresText.setText(album.genres);
		} else {
			genresText.setVisibility(View.GONE);
		}
		// asynchronously load the cover
        HttpApiThread.music().getAlbumCover(new HttpApiHandler<Bitmap>(activity) {

			@Override
			public void doFinish(Bitmap value) {
				if (value == null) {
        			cover.setImageResource(R.drawable.nocover);
        		} else {
        			cover.setImageBitmap(value);
        		}
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				
			}
        }, album, ThumbSize.big);
        
		cover.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getTrackDetail(activity, album).show();
			}
		});
		return dialog;
	}
	
	/**
	 * Returns the track listing of an album.
	 * @param activity
	 * @param album
	 * @return Track listing dialog
	 */
	public static Dialog getTrackDetail(final Activity activity, final Album album) {
		
		Dialog dialog = new Dialog(activity);
		dialog.setContentView(R.layout.albumtracks);
		dialog.setTitle(album.name);

		// get controls
		TextView artistText = (TextView) dialog.findViewById(R.id.album_artistname);
		ImageView cover = (ImageView)dialog.findViewById(R.id.album_cover);
		TextView numTrackText = (TextView) dialog.findViewById(R.id.album_numtracks);
		TextView yearText = (TextView) dialog.findViewById(R.id.album_year);
		TableLayout trackTable = (TableLayout) dialog.findViewById(R.id.album_tracktable);

		// update content
		artistText.setText(album.artist);
		if (album.year > 0) {
			yearText.setText(String.valueOf(album.year));
		} else {
			yearText.setVisibility(View.GONE);
		}
		
        final File file = new File(ImportUtilities.getCacheDirectory(album.getArtFolder(), ThumbSize.small), String.format("%08x", album.getCrc()).toLowerCase());
        if (file.exists()) {
        	cover.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
        
		trackTable.setScrollContainer(true);
		
		ArrayList<Song> songs = null;
		try {
			songs = ConnectionManager.getHttpClient(activity).music.getSongs(album);
		} catch (BackendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		numTrackText.setText(songs.size() + " Tracks");
		
		
		for (Song song: songs) {
			TableRow tr = new TableRow(activity);
			
			TextView txtTrack = new TextView(activity);
			txtTrack.setText(String.valueOf(song.track));
			txtTrack.setGravity(Gravity.RIGHT);
			txtTrack.setWidth(20);
			txtTrack.setPadding(0, 0, 5, 0);
			txtTrack.setTextSize(TypedValue.COMPLEX_UNIT_PX, 10);
			
			TextView txtTitle = new TextView(activity);
			if (album.isVA()) {
				txtTitle.setText(song.artist + " - " + song.title);
			} else {
				txtTitle.setText(song.title);
			}
			txtTitle.setWidth(200);
			
			TextView txtDuration = new TextView(activity);
			txtDuration.setText(song.getDuration());
			txtDuration.setGravity(Gravity.RIGHT);
			
			tr.addView(txtTrack);
			tr.addView(txtTitle);
			tr.addView(txtDuration);
			
			trackTable.addView(tr);
		}		
		
		return dialog;
	}
}
