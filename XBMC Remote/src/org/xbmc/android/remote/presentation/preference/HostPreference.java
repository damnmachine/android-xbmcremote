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

package org.xbmc.android.remote.presentation.preference;

import org.xbmc.android.remote.R;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * One of those contains name, host, port, user and pass of an XBMC instance.
 * 
 * @author Team XBMC
 */
public class HostPreference extends DialogPreference {
	
	private EditText mNameView;
	private EditText mHostView;
	private EditText mPortView;
	private EditText mUserView;
	private EditText mPassView;
//	private static int ITEM_CONTEXT_DELETE = 1;
	
	private Host mHost;
	
	public static final String ID_PREFIX = "settings_host_";

	public HostPreference(Context context) {
		this(context, null);
	}
	
	public HostPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.preference_host);
	}
	
	public void setHost(Host host) {
		mHost = host;
	}
	
	public Host getHost() {
		return mHost;
	}
	
	@Override
	protected View onCreateDialogView() {
		final ViewGroup parent = (ViewGroup)super.onCreateDialogView();
		mNameView = (EditText)parent.findViewById(R.id.pref_name);
		mHostView = (EditText)parent.findViewById(R.id.pref_host);
		mPortView = (EditText)parent.findViewById(R.id.pref_port);
		mUserView = (EditText)parent.findViewById(R.id.pref_user);
		mPassView = (EditText)parent.findViewById(R.id.pref_pass);
		return parent;
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
/*		if (mHost != null) {
			view.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					menu.setHeaderTitle(mHost.name);
					menu.add(0, ITEM_CONTEXT_DELETE, 1, "Remove instance");
				}
			});
		}*/
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		if (mHost != null) {
			mNameView.setText(mHost.name);
			mHostView.setText(mHost.host);
			mPortView.setText(String.valueOf(mHost.port));
			mUserView.setText(mHost.user);
			mPassView.setText(mHost.pass);
		}
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			final Host host = new Host();
			host.name = mNameView.getText().toString();
			host.host = mHostView.getText().toString();
			try {
				host.port = Integer.parseInt(mPortView.getText().toString());
			} catch (NumberFormatException e) {
				host.port = 0;
			}
			host.user = mUserView.getText().toString();
			host.pass = mPassView.getText().toString();
			
			if (mHost == null) {
				Host.addHost(getContext(), host);
			} else {
				host.id = mHost.id;
				Host.updateHost(getContext(), host);
			}
			if (callChangeListener(host)) {
				notifyChanged();
				setHost(host);
			}
		}
	}
}