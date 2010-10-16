// Dashboard: an Android front-end to the RightScale dashboard
// Copyright (C) 2009 Tony Spataro <code@tracker.xeger.net>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package net.xeger.rest;

public class RestException
	extends java.lang.Exception
{
	private static final long serialVersionUID = -5691717258605209067L;

	protected int _statusCode;
	
	public RestException(String message) {
		super(message);
		_statusCode = 0;
	}

	public RestException(Throwable cause) {
		super(cause);
		_statusCode = 0;
	}

	public RestException(String message, int statusCode) {
		super(message);
		_statusCode = statusCode;
	}
}
