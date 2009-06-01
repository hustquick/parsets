package edu.uncc.parsets.util.osabstraction;

import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeMapped;
import com.sun.jna.PointerType;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

import edu.uncc.parsets.ParallelSets;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * Copyright (c) 2009, Robert Kosara, Caroline Ziemkiewicz,
 *                     and others (see Authors.txt for full list)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of UNC Charlotte nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *      
 * THIS SOFTWARE IS PROVIDED BY ITS AUTHORS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

public class Windows extends AbstractOS {

	public Windows() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			ParallelSets.logger.info("Could not set Windows look and feel", e);
		}
	}
	
	// http://stackoverflow.com/questions/585534/what-is-the-best-way-to-find-the-users-home-directory-in-java
	// https://jna.dev.java.net/
	@Override
	public String getLocalDBDir() {
		if (com.sun.jna.Platform.isWindows()) {
			HWND hwndOwner = null;
			int nFolder = Shell32.CSIDL_APPDATA;
			HANDLE hToken = null;
			int dwFlags = Shell32.SHGFP_TYPE_CURRENT;
			char[] pszPath = new char[Shell32.MAX_PATH];
			int hResult = Shell32.INSTANCE.SHGetFolderPath(hwndOwner, nFolder, hToken, dwFlags, pszPath);
			if (Shell32.S_OK == hResult) {
				String path = new String(pszPath);
				int len = path.indexOf('\0');
				path = path.substring(0, len);
				return path;
			} else {
				ParallelSets.logger.fatal("Error determining Application Data directory: "+hResult);
				return null;
			}
		} else
			return null;
	}

    private static Map<String, Object> OPTIONS = new HashMap<String, Object>();
	static {
		OPTIONS.put(Library.OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
		OPTIONS.put(Library.OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
	}

	static class HANDLE extends PointerType implements NativeMapped {
	}

	static class HWND extends HANDLE {
	}

	static interface Shell32 extends Library {

		public static final int MAX_PATH = 260;

		public static final int CSIDL_LOCAL_APPDATA = 0x001c;
		
		public static final int CSIDL_APPDATA = 0x001a;
		
		public static final int CSIDL_PERSONAL = 0x0005;

		public static final int SHGFP_TYPE_CURRENT = 0;

		public static final int SHGFP_TYPE_DEFAULT = 1;

		public static final int S_OK = 0;

		static Shell32 INSTANCE = (Shell32) Native.loadLibrary("shell32", Shell32.class, OPTIONS);

		/**
		 * see http://msdn.microsoft.com/en-us/library/bb762181(VS.85).aspx
		 * 
		 * HRESULT SHGetFolderPath( HWND hwndOwner, int nFolder, HANDLE hToken,
		 * DWORD dwFlags, LPTSTR pszPath);
		 */
		public int SHGetFolderPath(HWND hwndOwner, int nFolder, HANDLE hToken, int dwFlags, char[] pszPath);
	}

	@Override
	public String shortName() {
		return "win";
	}

}
