package org.wycliffeassociates.translationrecorder.Playback.Editing;

import org.wycliffeassociates.translationrecorder.Playback.player.UIDataManager;
import org.wycliffeassociates.translationrecorder.Reporting.Logger;

/**
 * Created by sarabiaj on 11/20/2015.
 */
public class SectionMarkers {

    private static int minimapStart = Integer.MIN_VALUE;
    private static int minimapEnd = Integer.MAX_VALUE;
    private static boolean playSelectedSection = false;
    private static int mainEnd = Integer.MAX_VALUE;
    private static int mainStart = Integer.MIN_VALUE;
    private static boolean startSet = false;
    private static boolean endSet = false;

    public static boolean bothSet() {
        return playSelectedSection;
    }

    public static boolean shouldDrawMarkers(){
        return playSelectedSection || startSet || endSet;
    }

    public static int getMinimapMarkerStart() {
        return minimapStart;
    }

    public static int getMinimapMarkerEnd() {
        return minimapEnd;
    }

    public static void setPlaySelectedSection(boolean x){
        playSelectedSection = x;
    }

    public static void setStartTime(int x, int adjustedDuration, UIDataManager manager){
        manager.startSectionAt(x);
        mainStart = x;
        minimapStart = (int)((manager.reverseTimeAdjusted(x) / (double)adjustedDuration) * manager.getMinimapWidth());
        startSet = true;
        swapIfNeeded();
        if(startSet && endSet){
            playSelectedSection = true;
        }
    }

    public static void setEndTime(int x, int adjustedDuration, UIDataManager manager){
        //manager.startSectionAt(x); //This line wasn't here originally?
        mainEnd = x;
        minimapEnd = (int)((manager.reverseTimeAdjusted(x) / (double)adjustedDuration) * manager.getMinimapWidth());
        endSet = true;
        swapIfNeeded();
        if(startSet && endSet){
            playSelectedSection = true;
        }
    }

    public static void swapIfNeeded(){
        if(mainStart > mainEnd){
            int temp = mainEnd;
            mainEnd = mainStart;
            mainStart = temp;
            temp = minimapEnd;
            minimapEnd = minimapStart;
            minimapEnd = temp;
        }
    }

    public static void setMinimapMarkers(int start, int end){
        minimapStart = start;
        minimapEnd = end;
        playSelectedSection = true;
    }

    public static void setMainMarkers(int start, int end){
        mainStart = start;
        mainEnd = end;
    }

    public static int getStartLocationMs(){
        return mainStart;
    }

    public static int getEndLocationMs(){
        return mainEnd;
    }

    public static int getStartMarker(){
        int loc = (int)(getStartLocationMs()*88.2);
        return (loc % 2 == 0)? loc : loc + 1;
    }

    public static int getEndMarker(){
        int loc = (int)(getEndLocationMs()*88.2);
        return (loc % 2 == 0)? loc : loc + 1;
    }

    public static void clearMarkers(UIDataManager manager){
        Logger.i("static clear markers", "Cleared markers");
        playSelectedSection = false;
        endSet = false;
        startSet = false;
        minimapEnd = Integer.MAX_VALUE;
        minimapStart = Integer.MIN_VALUE;
        mainEnd = Integer.MAX_VALUE;
        mainStart = Integer.MIN_VALUE;
        manager.setOnlyPlayingSection(false);
    }
}
