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

package org.xbmc.httpapi;

import org.xbmc.api.business.INotifiableManager;
import org.xbmc.httpapi.client.ControlClient;
import org.xbmc.httpapi.client.InfoClient;
import org.xbmc.httpapi.client.MusicClient;
import org.xbmc.httpapi.client.VideoClient;


/**
 * Wrapper class for our HTTP clients. The idea is to separate the loads of
 * API method we're going to have into separate classes. The HttpClient class
 * instantiates them and keeps them in a central place, typically accessible by
 * the ConnectionManager. 
 * <p>
 * So in your code, all you have to do to access the API is getting a HttpClient
 * instance and access the clients:
 * <p>
 * <pre>
 *  HttpClient client = ConnectionManager.getHttpClient(this);
 *  String xbmcVersion = client.info.getSystemInfo(SystemInfo.SYSTEM_BUILD_VERSION);
 *  ArrayList&lt;Album&gt; albums = client.music.getAlbums()</pre>
 *   
 * Since the ConnectionManager keeps a static instance of the HttpClient, you
 * can also get the client without passing an activity once it has already been
 * used:
 * <p>
 * <pre>
 *  HttpClient client = ConnectionManager.getHttpClient();
 *</pre>
 * 
 * @author Team XBMC
 */
public class HttpClient {
	
	/**
	 * Use this client for anything system related
	 */
	public final InfoClient info;
	
	/**
	 * Use this client for anything music related
	 */
	public final MusicClient music;
	
	/**
	 * Use this client for anything video related
	 */
	public final VideoClient video;
	
	/**
	 * Use this client for anything media controller related
	 */
	public final ControlClient control;
	

	private final Connection mConnection;

	/**
	 * Construct with host only
	 * @param host         Host or IP address to XBMC
	 * @param manager      Notifiable manager
	 */
	public HttpClient(String host, int timeout, INotifiableManager manager) {
		this(host, -1, null, null, timeout, manager);
	}
	
	/**
	 * Construct with host and custom port
	 * @param host         Host or IP address to XBMC
	 * @param port         Port to the webserver
	 * @param manager      Notifiable manager
	 */
	public HttpClient(String host, int port, int timeout, INotifiableManager manager) {
		this(host, port, null, null, timeout, manager);
	}

	/**
	 * Construct with additional login credentials
	 * @param host         Host or IP address to XBMC
	 * @param username     Username
	 * @param password     Password
	 * @param manager      Notifiable manager
	 */
	public HttpClient(String host, String username, String password, int timeout, INotifiableManager manager) {
		this(host, -1, username, password, timeout, manager);
	}

	/**
	 * Construct with all paramaters
	 * @param host         Host or IP address to XBMC
	 * @param port         Port to XBMC's webserver
	 * @param username     Password
	 * @param password     Username
	 * @param manager      Notifiable manager
	 */
	public HttpClient(String host, int port, String username, String password, int timeout, INotifiableManager manager) {		
		Connection connection = new Connection(host, port, username, password, timeout, manager);
		info = new InfoClient(connection);
		music = new MusicClient(connection);
		video = new VideoClient(connection);
		control = new ControlClient(connection);
		mConnection = connection;
	}

	public boolean isConnected() {
		return mConnection.isConnected();
	}
}
