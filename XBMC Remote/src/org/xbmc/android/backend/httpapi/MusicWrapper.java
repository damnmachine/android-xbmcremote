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

package org.xbmc.android.backend.httpapi;

import java.util.ArrayList;

import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.data.Album;
import org.xbmc.httpapi.data.Artist;
import org.xbmc.httpapi.data.Genre;
import org.xbmc.httpapi.data.ICoverArt;
import org.xbmc.httpapi.data.Song;
import org.xbmc.httpapi.info.GuiSettings;
import org.xbmc.httpapi.type.CacheType;
import org.xbmc.httpapi.type.ThumbSize;

import android.graphics.Bitmap;
import android.util.Log;


/**
 * Asynchronously wraps the {@link org.xbmc.httpapi.client.InfoClient} class.
 * 
 * TODO All the asynchronous cover download stuff needs to be abstracted to an
 *      interface so we can use it for video as well.  
 * 
 * @author Team XBMC
 */
public class MusicWrapper extends Wrapper {
	
	private static final String TAG = "MusicWrapper";
	private static final Boolean DEBUG = false;
	
	/**
	 * Gets all albums from database
	 * @param handler Callback handler
	 */
	public void getCompilations(final HttpApiHandler<ArrayList<Album>> handler) {
		mHandler.post(new Runnable() {
			public void run() {
				final MusicClient mc = music(handler);
				ArrayList<Integer> compilationArtistIDs = mc.getCompilationArtistIDs();
				handler.value = mc.getAlbums(compilationArtistIDs);
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all albums from database
	 * @param handler Callback handler
	 */
	public void getAlbums(final HttpApiHandler<ArrayList<Album>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getAlbums();
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all albums of an artist from database
	 * @param handler Callback handler
	 * @param artist  Artist of the albums
	 */
	public void getAlbums(final HttpApiHandler<ArrayList<Album>> handler, final Artist artist) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getAlbums(artist);
				done(handler);
			}
		});
	}

	/**
	 * Gets all albums of a genre from database
	 * @param handler Callback handler
	 * @param artist  Genre of the albums
	 */
	public void getAlbums(final HttpApiHandler<ArrayList<Album>> handler, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getAlbums(genre);
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all songs of an album from database
	 * @param handler Callback handler
	 * @param album The album
	 */
	public void getSongs(final HttpApiHandler<ArrayList<Song>> handler, final Album album) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getSongs(album);
				done(handler);
			}
		});
	}

	/**
	 * Gets all songs from an artist from database
	 * @param handler Callback handler
	 * @param album The artist
	 */
	public void getSongs(final HttpApiHandler<ArrayList<Song>> handler, final Artist artist) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getSongs(artist);
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all songs of a genre from database
	 * @param handler Callback handler
	 * @param album The genre
	 */
	public void getSongs(final HttpApiHandler<ArrayList<Song>> handler, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getSongs(genre);
				done(handler);
			}
		});
	}

	/**
	 * Gets all artists from database
	 * @param handler Callback handler
	 */
	public void getArtists(final HttpApiHandler<ArrayList<Artist>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				final boolean albumArtistsOnly = info(handler).getGuiSettingBool(GuiSettings.MusicLibrary.ALBUM_ARTISTS_ONLY);
				handler.value = music(handler).getArtists(albumArtistsOnly);
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all artists with at least one song of a genre.
	 * @param handler Callback handler
	 */
	public void getArtists(final HttpApiHandler<ArrayList<Artist>> handler, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				final boolean albumArtistsOnly = info(handler).getGuiSettingBool(GuiSettings.MusicLibrary.ALBUM_ARTISTS_ONLY);
				handler.value = music(handler).getArtists(genre, albumArtistsOnly);
				done(handler);
			}
		});
	}
	
	/**
	 * Gets all artists from database
	 * @param handler Callback handler
	 */
	public void getGenres(final HttpApiHandler<ArrayList<Genre>> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).getGenres();
				done(handler);
			}
		});
	}
	
	/**
	 * Adds an album to the current playlist. If current playlist is stopped,
	 * the album is added to playlist and the first song is selected to play. 
	 * If something is playing already, the album is only queued.
	 * 
	 * @param handler Callback
	 * @param album Album to add
	 */
	public void addToPlaylist(final HttpApiHandler<Boolean> handler, final Album album) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient cc = control(handler);
				final int numAlreadyQueued = mc.getPlaylistSize();
				handler.value = mc.addToPlaylist(album);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(handler);
			}
		});
	}
	
	/**
	 * Adds all songs of a genre to the current playlist. If current playlist is stopped,
	 * play is executed. Value is the first song of the added album.
	 * @param handler Callback
	 * @param genre Genre of songs to add
	 */
	public void addToPlaylist(final HttpApiHandler<Boolean> handler, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient cc = control(handler);
				final int numAlreadyQueued = mc.getPlaylistSize();
				handler.value = mc.addToPlaylist(genre);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(handler);
			}
		});
	}
	
	/**
	 * Adds a song to the current playlist. Even if the playlist is empty, only this song will be added.
	 * @param handler Callback
	 * @param album Song to add
	 */
	public void addToPlaylist(final HttpApiHandler<Boolean> handler, final Song song) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).addToPlaylist(song);
				done(handler);
			}
		});
	}

	/**
	 * Adds a song to the current playlist. If the playlist is empty, the whole
	 * album will be added with this song playing, otherwise only this song is
	 * added.
	 * @param handler Callback
	 * @param album Album to add
	 * @param song Song to play
	 */
	public void addToPlaylist(final HttpApiHandler<Boolean> handler, final Album album, final Song song) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient.PlayStatus ps = control(handler).getPlayState();
				if (mc.getPlaylistSize() == 0) {  // if playlist is empty, add the whole album
					int n = 0;
					int playPos = 0;
					for (Song albumSong : mc.getSongs(album)) {
						if (albumSong.id == song.id) {
							playPos = n;
						}
						n++;
						mc.addToPlaylist(albumSong);
					}
					mc.setCurrentPlaylist();
					mc.playlistSetSong(playPos);
				} else {                          // otherwise, only add the song
					handler.value = mc.addToPlaylist(song);
				}
				if (ps == ControlClient.PlayStatus.Stopped) { // if nothing is playing, play the song
					mc.setCurrentPlaylist();
					handler.value = mc.play(song);
				}
				done(handler);
			}
		});
	}

	/**
	 * Adds all songs from an artist to the playlist. If current playlist is
	 * stopped, the all songs of the artist are added to playlist and the first
	 * song is selected to play. If something is playing already, the songs are
	 * only queued.
	 * @param handler Callback
	 * @param artist 
	 */
	public void addToPlaylist(final HttpApiHandler<Boolean> handler, final Artist artist) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient cc = control(handler);
				final int numAlreadyQueued = mc.getPlaylistSize();
				handler.value = mc.addToPlaylist(artist);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(handler);
			}
		});
	}

	/**
	 * Adds all songs of a genre from an artist to the playlist. If nothing is playing, 
	 * the first song will be played, otherwise songs are just added to the playlist.
	 * @param handler Callback
	 * @param artist 
	 * @param genre 
	 */
	public void addToPlaylist(final HttpApiHandler<Boolean> handler, final Artist artist, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient cc = control(handler);
				final int numAlreadyQueued = mc.getPlaylistSize();
				handler.value = mc.addToPlaylist(artist, genre);
				checkForPlayAfterQueue(mc, cc, numAlreadyQueued);
				done(handler);
			}
		});
	}
	
	/**
	 * Sets the media at playlist position position to be the next item to be played.
	 * @param handler Callback
	 * @param position Position, starting with 0.
	 */
	public void setPlaylistSong(final HttpApiHandler<Boolean> handler, final int position) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).setPlaylistPosition(position);
				done(handler);
			}
		});
	}
	
	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Position to remove, starting with 0.
	 * @return True on success, false otherwise.
	 */
	public void removeFromPlaylist(final HttpApiHandler<Boolean> handler, final int position) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).removeFromPlaylist(position);
				done(handler);
			}
		});
	}

	/**
	 * Removes media from the current playlist. It is not possible to remove the media if it is currently being played.
	 * @param position Complete path (including filename) of the media to be removed.
	 * @return True on success, false otherwise.
	 */
	public void removeFromPlaylist(final HttpApiHandler<Boolean> handler, final String path) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).removeFromPlaylist(path);
				done(handler);
			}
		});
	}
	
	/**
	 * Plays an album
	 * @param handler Callback
	 * @param album Album to play
	 */
	public void play(final HttpApiHandler<Boolean> handler, final Album album) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).play(album);
				done(handler);
			}
		});
	}
	
	/**
	 * Plays all songs of a genre
	 * @param handler Callback
	 * @param genre Genre of songs to play
	 */
	public void play(final HttpApiHandler<Boolean> handler, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).play(genre);
				done(handler);
			}
		});
	}
	
	/**
	 * Plays a song
	 * @param handler Callback
	 * @param song Song to play
	 */
	public void play(final HttpApiHandler<Boolean> handler, final Song song) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).play(song);
				done(handler);
			}
		});
	}
	
	/**
	 * Plays a song, but the whole album is added to the playlist.
	 * @param handler Callback
	 * @param album Album to queue
	 * @param song Song to play
	 */
	public void play(final HttpApiHandler<Boolean> handler, final Album album, final Song song) {
		mHandler.post(new Runnable() {
			public void run() { 
				final MusicClient mc = music(handler);
				final ControlClient cc = control(handler);
				int n = 0;
				int playPos = 0;
				mc.clearPlaylist();
				for (Song albumSong : mc.getSongs(album)) {
					if (albumSong.id == song.id) {
						playPos = n;
					}
					n++;
					mc.addToPlaylist(albumSong);
				}
				cc.stop();
				mc.setCurrentPlaylist();
				mc.playlistSetSong(playPos);
				done(handler);
			}
		});
	}

	/**
	 * Plays all songs from an artist
	 * @param handler Callback
	 * @param artist Artist whose songs to play
	 */
	public void play(final HttpApiHandler<Boolean> handler, final Artist artist) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).play(artist);
				done(handler);
			}
		});
	}
	
	/**
	 * Plays songs of a genre from an artist
	 * @param handler Callback
	 * @param artist Artist whose songs to play
	 * @param genre  Genre filter
	 */
	public void play(final HttpApiHandler<Boolean> handler, final Artist artist, final Genre genre) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).play(artist, genre);
				done(handler);
			}
		});
	}
	
	
	/**
	 * Starts playing the next media in the current playlist. 
	 * @param handler Callback
	 */
	public void playlistNext(final HttpApiHandler<Boolean> handler) {
		mHandler.post(new Runnable() {
			public void run() { 
				handler.value = music(handler).playlistNext();
				done(handler);
			}
		});
	}
	
	/**
	 * Returns an array of songs on the playlist. Empty array if nothing is playing.
	 * @param handler Callback
	 */
	public void getPlaylist(final HttpApiHandler<ArrayList<String>> handler) {
		mHandler.post(new Runnable() {
			public void run() {
				handler.value = music(handler).getPlaylist();
				done(handler);
			}
		});
	}
	
	/**
	 * Returns the position of the currently playing song in the playlist. First position is 0.
	 * @param handler Callback
	 */
	public void getPlaylistPosition(final HttpApiHandler<Integer> handler) {
		mHandler.post(new Runnable() {
			public void run() {
				handler.value = music(handler).getPlaylistPosition();
				done(handler);
			}
		});
	}
	
	/**
	 * Returns bitmap of an album cover. Note that the callback is done by the
	 * helper methods below.
	 * @param handler Callback handler
	 */
	public void getAlbumCover(final HttpApiHandler<Bitmap> handler, final ICoverArt album, final ThumbSize size) {
		mHandler.post(new Runnable() {
			public void run() {
				// first, try mem cache (only if size = small, other sizes aren't mem-cached.
				if (size == ThumbSize.small) {
					getAlbumCoverFromMem(handler, album);
				} else {
					getAlbumCoverFromDisk(handler, album, size);
				}
			}
		});
	}
	
	/**
	 * Checks if something's playing. If that's not the case, set the 
	 * playlist's play position either to the start if there were no items
	 * before, or to the first position of the newly added files.
	 * @param mc Music client
	 * @param cc Control client
	 * @param numAlreadyQueued Number of previously queued items
	 */
	private void checkForPlayAfterQueue(final MusicClient mc, final ControlClient cc, int numAlreadyQueued) {
		final ControlClient.PlayStatus ps = cc.getPlayState();
		if (ps == ControlClient.PlayStatus.Stopped) { // if nothing is playing, play the song
			mc.setCurrentPlaylist();
			if (numAlreadyQueued == 0) {
				mc.playlistNext();
			} else {
				mc.playlistSetSong(numAlreadyQueued);
			}
		}
	}
	
	/**
	 * Tries to get small cover from memory, then from disk, then download it from XBMC.
	 * @param handler Callback handler
	 * @param album   Get cover for this album
	 */
	private void getAlbumCoverFromMem(final HttpApiHandler<Bitmap> handler, final ICoverArt album) {
		if (DEBUG) Log.i(TAG, "[" + album.getId() + "] Checking in mem cache..");
		HttpApiMemCacheThread.get().getCover(new HttpApiHandler<Bitmap>(handler.getActivity()) {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + album.getId() + " empty]");
					// then, try sdcard cache
					getAlbumCoverFromDisk(handler, album, ThumbSize.small);
				} else {
					if (DEBUG) Log.i(TAG, "[" + album.getId() + " FOUND in memory!]");
					handler.value = value;
					handler.setCacheType(CacheType.memory);
					done(handler);
				}
			}
		}, album);
	}
	
	/**
	 * Tries to get cover from disk, then download it from XBMC.
	 * @param handler Callback handler
	 * @param album   Get cover for this album
	 * @param size    Cover size
	 */
	private void getAlbumCoverFromDisk(final HttpApiHandler<Bitmap> handler, final ICoverArt album, final ThumbSize size) {
		if (DEBUG) Log.i(TAG, "[" + album.getId() + "] Checking in disk cache..");
		HttpApiDiskCacheThread.get().getCover(new HttpApiHandler<Bitmap>(handler.getActivity()) {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + album.getId() + " empty]");
					if (handler.postCache()) {
						// well, let's download
						getAlbumCoverFromNetwork(handler, album, size);
					}
				} else {
					if (DEBUG) Log.i(TAG, "[" + album.getId() + " FOUND on disk!]");
					handler.value = value;
					handler.setCacheType(CacheType.sdcard);
					done(handler);
				}
			}
		}, album, size);		
	}
	
	/**
	 * Last stop: try to download from XBMC.
	 * @param handler Callback handler
	 * @param album   Get cover for this album
	 * @param size    Cover size
	 */
	private void getAlbumCoverFromNetwork(final HttpApiHandler<Bitmap> handler, final ICoverArt album, final ThumbSize size) {
		if (DEBUG) Log.i(TAG, "[" + album.getId() + "] Downloading..");
		HttpApiDownloadThread.get().getCover(new HttpApiHandler<Bitmap>(handler.getActivity()) {
			public void run() {
				if (value == null) {
					if (DEBUG) Log.i(TAG, "[" + album.getId() + " empty]");
				} else {
					if (DEBUG) Log.i(TAG, "[" + album.getId() + " DOWNLOADED!]");
					handler.setCacheType(CacheType.network);
					handler.value = value;
				}
				done(handler); // callback in any case, since we don't go further than that.
			}
		}, album, size);
	}

}