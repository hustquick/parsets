package edu.uncc.parsets.util.osabstraction;

import java.io.File;

import edu.uncc.parsets.ParallelSets;
import edu.uncc.parsets.data.LocalDB;

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

public class Linux extends AbstractOS {

	private static final String DOTDIRNAME = ".parsets";

	/**
	 * Installs into .parsets in the user's home directory. The program can be
	 * run from anywhere on the system.
	 */
	@Override
	public void install(File dbFile) {
		ParallelSets.logger.info("Installing new database at "
				+ dbFile.getAbsolutePath());
		File parentDir = dbFile.getParentFile();
		if (!parentDir.exists())
			if (parentDir.mkdir() == false)
				ParallelSets.logger.fatal("Could not create parent directory");
		copyFile(new File(LocalDB.LOCALDBFILENAME), dbFile);
	}

	@Override
	public String getLocalDBPath(String dbFileName) {
		String dbPath = getLocalDBDir() + File.separatorChar + DOTDIRNAME
				+ File.separatorChar + dbFileName;
		File dbFile = new File(dbPath);
		if (!dbFile.exists())
			install(dbFile);
		return dbPath;
	}

	@Override
	public String getLocalDBDir() {
		return System.getProperty("user.home");
	}

	@Override
	public String shortName() {
		return "lnx";
	}
}
