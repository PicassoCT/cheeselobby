/*
 *  Copyright 2010 Jahziah Wagner <jahziah[dot]wagner[at]gmail.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package net.cheesecan.cheeselobby.unitsync;

import com.sun.jna.Pointer;
import unitsync.MapInfo;

public class LobbyMapInfo {
	public Pointer mDescription;
	public int mTidalStrength;
	public int mGravity;
	public float mMaxMetal;
	public int mExtractorRadius;
	public int mMinWind;
	public int mMaxWind;
	public int mWidth;
	public int mHeight;
	public int[] mXPositions;
	public int[] mZPositions;
	public String mAuthor;

	public  LobbyMapInfo(MapInfo mapinfo) {
		mDescription = mapinfo.description;
		mTidalStrength = mapinfo.tidalStrength;
		mGravity = mapinfo.gravity;
		mMaxMetal = mapinfo.maxMetal;
		mExtractorRadius = mapinfo.extractorRadius;
		mMinWind = mapinfo.minWind;
		mMaxWind = mapinfo.maxWind;
		mWidth = mapinfo.width;
		mHeight = mapinfo.height;
		mXPositions = mapinfo.positions[0].getPointer().getIntArray(0, mapinfo.posCount);
		mZPositions = mapinfo.positions[1].getPointer().getIntArray(0, mapinfo.posCount);
	}

	public String toString() {
		String s = "";
		s += "Description      " + mDescription + "\n";
		s += "TidalStrength    " + mTidalStrength + "\n";
		s += "Gravity          " + mGravity + "\n";
		s += "MaxMetal         " + mMaxMetal + "\n";
		s += "ExtractionRadius " + mExtractorRadius + "\n";
		s += "MinWind          " + mMinWind + "\n";
		s += "MaxWind          " + mMaxWind + "\n";
		s += "Width            " + mWidth + "\n";
		s += "Height           " + mHeight + "\n";
		s += "Positions\n";
		for (int i = 0; i < mXPositions.length; i++) {
			s += "  " + (mXPositions[i]) + "," + (mZPositions[i]) + "\n";
		}
		if (mAuthor != null)
			s += "Author           " + mAuthor + "\n";
		return s;
	}
}