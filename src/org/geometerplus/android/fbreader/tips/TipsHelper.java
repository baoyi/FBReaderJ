/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.tips;

import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.network.atom.ATOMEntry;
import org.geometerplus.fbreader.network.atom.ATOMFeedMetadata;
import org.geometerplus.fbreader.network.atom.ATOMFeedHandler;
import org.geometerplus.fbreader.network.atom.ATOMXMLReader;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;

import android.util.Log;

public class TipsHelper {
	private static String TIPS_PATH;
	private ITipFeedListener myTipFeedListener;
	
	public TipsHelper(ITipFeedListener tipFeedListener) {
		Log.v(TipsKeys.TIPS_LOG, "TipsHelper was created");
		myTipFeedListener = tipFeedListener;
		TIPS_PATH = Paths.networkCacheDirectory()+"/tips/tips.xml";	
	}

	public void showTip() {
		boolean isShowTips = getShowOption().getValue();
		if (isShowTips) {
			getDateOption().setValue(new Date().getDate());
			tryShowTip();
		}
	}
	
	private final int maxCountTips = 10;
	private void tryShowTip() {
		int currId = -1;
		ZLFile tipsFile = ZLFile.createFileByPath(TIPS_PATH);
		if (tipsFile.exists()) {
			ZLIntegerOption idOpt = new ZLIntegerOption(TipsKeys.OPTION_GROUP, TipsKeys.CURR_TIP_ID, 0);
			currId = idOpt.getValue();
			if (currId >= maxCountTips) {
				idOpt.setValue(0);
				tipsFile.getPhysicalFile().delete();
				
				Random random = new Random();
				currId = 1 + random.nextInt(10);
				tipsFile = getDefaultTipsFile();
			} else {
				currId++;
				idOpt.setValue(currId);
			}
		} else {
			Random random = new Random();
			currId = 1 + random.nextInt(10);
			tipsFile = getDefaultTipsFile();
		}
		
		new ATOMXMLReader(new TipsATOMFeedHandler(currId), false).read(tipsFile);
	}
	
	private ZLFile getDefaultTipsFile() {
		return ZLResourceFile.createResourceFile("tips/tips." + Locale.getDefault().getLanguage() + ".xml");		
	}

	public static ZLBooleanOption getShowOption(){
		return new ZLBooleanOption(TipsKeys.OPTION_GROUP, TipsKeys.SHOW_TIPS, true);
	}
	
	public static ZLIntegerOption getDateOption(){
		return new ZLIntegerOption(TipsKeys.OPTION_GROUP, TipsKeys.LAST_TIP_DATE, 0);
	}
	
	private class TipsATOMFeedHandler implements ATOMFeedHandler {
		int myTipId;
		TipsATOMFeedHandler(int tipId) {
			myTipId = tipId;
		}

		int myCount = 1;
		@Override
		public boolean processFeedEntry(ATOMEntry entry) {
			if (myCount == myTipId) {
				Tip tip = new Tip(entry);
				myTipFeedListener.tipFound(tip);
				return true;
			}
			myCount++;
			return false;
		}

		@Override
		public void processFeedStart() {
		}
		
		@Override
		public void processFeedEnd() {
		}

		@Override
		public boolean processFeedMetadata(ATOMFeedMetadata feed, boolean beforeEntries) {
			return false;
		}
		
	}
	
	public class Tip {
		private ATOMEntry myEntry;
		
		Tip(ATOMEntry entry) {
			myEntry = entry;
		}

		public CharSequence getTipTitle() {
			return myEntry.Title;
		}
		
		public CharSequence getTipContent() {
			return myEntry.Content;
		}
	}
	
	public interface ITipFeedListener {
		void tipFound(Tip tip);
	}
}
