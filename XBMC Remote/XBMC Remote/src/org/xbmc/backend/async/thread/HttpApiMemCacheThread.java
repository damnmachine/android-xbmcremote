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

package org.xbmc.backend.async.thread;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import org.xbmc.android.remote.R;
import org.xbmc.backend.HttpApiHandler;
import org.xbmc.model.ICoverArt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * This thread asynchronously delivers memory-cached bitmaps.
 * 
 * The memory cache keeps small-size thumb bitmaps in a soft-referenced list.
 * This thread is directly accessed by the original HttpApi thread, through one
 * of its wrappers.
 * 
 * @author Team XBMC
 */
public class HttpApiMemCacheThread extends HttpApiAbstractThread {
	
	/**
	 * Singleton instance of this thread
	 */
	protected static HttpApiMemCacheThread sHttpApiThread;
	
	/**
	 * The actual cache variable. Here are the thumbs stored. 
	 */
	private static final HashMap<Long, SoftReference<Bitmap>> sArtCache = new HashMap<Long, SoftReference<Bitmap>>();
	private static final HashMap<Long, Boolean> sNotAvailable = new HashMap<Long, Boolean>();

	/**
	 * Constructor is protected, use get().
	 */
	protected HttpApiMemCacheThread() {
		super("HTTP API Mem Cache Thread");
	}
	
	/**
	 * Asynchronously returns a thumb from the mem cache, or null if 
	 * not available.
	 * 
	 * @param handler Callback
	 * @param cover   Which cover to return
	 */
	public void getCover(final HttpApiHandler<Bitmap> handler, final ICoverArt cover) {
		mHandler.post(new Runnable() {
			public void run() {
				if (cover != null) {
					final long crc = cover.getCrc();
					final SoftReference<Bitmap> ref = sArtCache.get(crc);
			        if (ref != null) {
			            handler.value = ref.get();
			        } else if (sNotAvailable.containsKey(crc)) {
//		            	Log.i("HttpApiMemCacheThread", "Delivering not-available image directly from cache (" + crc + ").");
		            	handler.value = BitmapFactory.decodeResource(handler.getActivity().getResources(), R.drawable.icon_album);
			        }
				}
				done(handler);
			}
		});
	}
	
	/**
	 * Synchronously returns a thumb from the mem cache, or null 
	 * if not available.
	 * 
	 * @param cover Which cover to return
	 * @return Bitmap or null if not available.
	 */
	public static Bitmap getCover(ICoverArt cover) {
		return sArtCache.get(cover.getCrc()).get();
	}
	
	/**
	 * Checks if a thumb is in the mem cache.
	 * @param cover
	 * @return True if thumb is in mem cache, false otherwise.
	 */
	public static boolean isInCache(ICoverArt cover) {
		return sArtCache.containsKey(cover.getCrc());
	}
	
	/**
	 * Adds a cover to the mem cache
	 * @param cover  Which cover to add
	 * @param bitmap Bitmap data
	 */
	public static void addCoverToCache(ICoverArt cover, Bitmap bitmap) {
		// if bitmap is null, add an entry to the sNotAvailable table so we can return the default bitmap later directly.
		if (bitmap == null) {
			sNotAvailable.put(cover.getCrc(), true);
		} else {
			sArtCache.put(cover.getCrc(), new SoftReference<Bitmap>(bitmap));
		}
	}

	/**
	 * Returns an instance of this thread. Spawns if necessary.
	 * @return
	 */
	public static HttpApiMemCacheThread get() {
		if (sHttpApiThread == null) {
 			sHttpApiThread = new HttpApiMemCacheThread();
			sHttpApiThread.start();
			// thread must be entirely started
			waitForStartup(sHttpApiThread);
		}
		return sHttpApiThread;
	}
}