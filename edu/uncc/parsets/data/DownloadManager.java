package edu.uncc.parsets.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.zip.GZIPInputStream;

import edu.uncc.parsets.gui.OnlineDataTab;
import edu.uncc.parsets.util.PSLogging;

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

public class DownloadManager extends Thread {

	LinkedBlockingDeque<OnlineDataSet> queue;
	private OnlineDataTab tab;
	private OnlineDataSet currentDS;
	
	public DownloadManager(OnlineDataTab onlineDataTab) {
		tab = onlineDataTab;
		queue = new LinkedBlockingDeque<OnlineDataSet>();
		setDaemon(true);
	}
	
	public void queueDownload(OnlineDataSet ds) {
		try {
			queue.put(ds);
		} catch (InterruptedException e) {
			// this queue is not limited, so there is no wait;
			// and where there is no wait, it can't be interrupted
		}
	}
	
	@Override
	public void run() {
		byte buffer[] = new byte[4096];
		while (true) {
			try {
				currentDS = queue.take();
				if (tab != null)
					tab.activateProgressBar(true);
				PSLogging.logger.info("Downloading dataset from "+currentDS.getURL());
				InputStream stream = (new URL(currentDS.getURL())).openStream();
				File tmpFile = File.createTempFile(currentDS.getHandle(), ".json.gz");
				tmpFile.deleteOnExit();
				FileOutputStream out = new FileOutputStream(tmpFile);
				boolean keepReading = true;
				while (keepReading) {
					int bytes = stream.read(buffer);
					if (bytes > 0)
						out.write(buffer, 0, bytes);
					else
						keepReading = false;
				}
				stream.close();
				out.close();
				
				Reader fileReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(tmpFile)));
				if (!tmpFile.delete())
					PSLogging.logger.warn("Could not delete temp file "+tmpFile.getAbsolutePath());

				JSONImport.importDataSet(LocalDB.getDefaultDB(), fileReader);
				LocalDB.getDefaultDB().rescanDB();

				if (tab != null && queue.isEmpty())
					tab.activateProgressBar(false);

			} catch (InterruptedException e) {
				// ignore
			} catch (Exception e) {
				PSLogging.logger.error("Error downloading dataset", e);
			}
		}
	}
	
	public boolean isDSqueued(DataSet ds) {
		return queue.contains(ds) || (currentDS == ds);
	}

}
