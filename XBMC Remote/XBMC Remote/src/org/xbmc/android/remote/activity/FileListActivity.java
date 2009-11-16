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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xbmc.android.util.ConnectionManager;
import org.xbmc.android.util.ErrorHandler;
import org.xbmc.backend.HttpApiHandler;
import org.xbmc.backend.HttpApiHandlerAdapter;
import org.xbmc.backend.async.thread.HttpApiThread;
import org.xbmc.backend.eventclient.ButtonCodes;
import org.xbmc.backend.eventclient.EventClient;
import org.xbmc.backend.httpapi.type.MediaType;
import org.xbmc.model.MediaLocation;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileListActivity extends ListActivity {
	
	public static final int MESSAGE_HANDLE_DATA = 1;
	public static final int MESSAGE_CONNECTION_ERROR = 2;
	
	private HashMap<String, MediaLocation> mFileItems;
	private volatile String mGettingUrl;
	private MediaType mMediaType;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ErrorHandler.setActivity(this);
		final String st = getIntent().getStringExtra("shareType");
		mMediaType = st != null ? MediaType.valueOf(st) : MediaType.music;
		final String path = getIntent().getStringExtra("path");
		fillUp(path == null ? "" : path);
	}

	@Override
	protected void onListItemClick(ListView l, final View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (mFileItems == null)
			return;

		MediaLocation item = mFileItems.get(l.getAdapter().getItem(position));
		if (item.isDirectory) {
			Intent nextActivity = new Intent(this, FileListActivity.class);
			nextActivity.putExtras(getIntent().getExtras());
			nextActivity.putExtra("shareType", mMediaType.toString());
			nextActivity.putExtra("path", item.path);
			startActivityForResult(nextActivity, 0);
		} else {
			HttpApiThread.control().playFile(new HttpApiHandler<Boolean>(this) {

				@Override
				public void doFinish(Boolean value) {
					if (value) {
						startActivityForResult(new Intent(v.getContext(), NowPlayingActivity.class), 0);
					}
				}

				@Override
				public void onError(Exception e) {
					// TODO Auto-generated method stub
					
				}
			}, item.path);
		}
	}

	private void fillUp(String url) {
		if (mGettingUrl != null)
			return;
		
		mGettingUrl = url;
		mFileItems = null;
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{ "Loading..." }));
		getListView().setTextFilterEnabled(false);
		
		HttpApiHandler<ArrayList<MediaLocation>> mediaListHandler = new HttpApiHandler<ArrayList<MediaLocation>>(this) {

			@Override
			public void doFinish(ArrayList<MediaLocation> value) {
				ArrayList<String> presentationList = new ArrayList<String>();
				mFileItems = new HashMap<String, MediaLocation>();
				
				for (MediaLocation item : value) {
					presentationList.add(item.name);
					mFileItems.put(item.name, item);
				}
				setListAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, presentationList));
				getListView().setTextFilterEnabled(true);
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				
			}
		};
		
		if (mGettingUrl.length() == 0) {
			HttpApiThread.info().getShares(mediaListHandler, mMediaType);
		} else {
			HttpApiThread.info().getDirectory(mediaListHandler, mGettingUrl);
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		/*if (mMediaType.equals(MediaType.music))
			menu.add(0, 0, 0, "Change view").setIcon(android.R.drawable.ic_menu_view);*/
		if (!mMediaType.equals(MediaType.music))
			menu.add(0, 1, 0, "Music");
		if (!mMediaType.equals(MediaType.video))
			menu.add(0, 2, 0, "Video");
		if (!mMediaType.equals(MediaType.pictures))
			menu.add(0, 3, 0, "Pictures").setIcon(android.R.drawable.ic_menu_camera);
		
		menu.add(0, 4, 0, "Now Playing").setIcon(android.R.drawable.ic_media_play);
		menu.add(0, 5, 0, "Remote");
		
		if (mMediaType.equals(MediaType.music) || mMediaType.equals(MediaType.video))
			menu.add(0, 6, 0, "Update Library");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent = null;
		
		switch (item.getItemId()) {
		case 0:
//			myIntent = new Intent(this, AlbumGridActivity.class);
			break;
		case 1:
			myIntent = new Intent(this, FileListActivity.class);
			myIntent.putExtra("shareType", MediaType.music.toString());
			break;
		case 2:
			myIntent = new Intent(this, FileListActivity.class);
			myIntent.putExtra("shareType", MediaType.video.toString());
			break;
		case 3:
			myIntent = new Intent(this, FileListActivity.class);
			myIntent.putExtra("shareType", MediaType.pictures.toString());
			break;
		case 4:
			myIntent = new Intent(this, NowPlayingActivity.class);
			break;
		case 5:
			myIntent = new Intent(this, RemoteActivity.class);
			break;
		case 6:
			HttpApiThread.control().updateLibrary(new HttpApiHandlerAdapter<Boolean>(this), mMediaType.toString());
			break;
		}
		
		if (myIntent != null) {
			startActivity(myIntent);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		EventClient client = ConnectionManager.getEventClient(this);	
		try {
			switch (keyCode) {
				case KeyEvent.KEYCODE_VOLUME_UP:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_PLUS, false, true, true, (short)0, (byte)0);
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN:
					client.sendButton("R1", ButtonCodes.REMOTE_VOLUME_MINUS, false, true, true, (short)0, (byte)0);
					return true;
			}
		} catch (IOException e) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
}