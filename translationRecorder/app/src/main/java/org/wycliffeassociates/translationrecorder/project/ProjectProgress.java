package org.wycliffeassociates.translationrecorder.project;


import android.content.Context;
import com.door43.tools.reporting.Logger;
import org.wycliffeassociates.translationrecorder.chunkplugin.Chapter;
import org.wycliffeassociates.translationrecorder.chunkplugin.ChunkPlugin;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by mXaln on 7/31/2019.
 */
public class ProjectProgress {

    Project project;
    Context context;
    ProjectDatabaseHelper db;
    ChunkPlugin chunks;

    public ProjectProgress(Project project, Context context) {
        this.project = project;
        this.context = context;
        this.db = new ProjectDatabaseHelper(context);

        try {
            this.chunks = project.getChunkPlugin(new ChunkPluginLoader(context));
        } catch (IOException e) {
            Logger.e(this.toString(), e.getMessage());
        }
    }

    public int calculateProjectProgress() {
        if(chunks != null) {
            int numChapters = chunks.numChapters();
            int allChaptersProgress = 0;

            List<Chapter> chapters = chunks.getChapters();
            for (Chapter chapter: chapters) {
                int chapterProgress = calculateChapterProgress(chapter);
                allChaptersProgress += chapterProgress;
            }
            int projectProgress = (int) Math.ceil((float) allChaptersProgress / numChapters);

            return projectProgress;
        }

        return 0;
    }

    public void setProjectProgress(int progress) {
        try {
            int projectId = db.getProjectId(project);
            db.setProjectProgress(projectId, progress);
        } catch (IllegalArgumentException e) {
            Logger.i(this.toString(), e.getMessage());
        }
    }

    public void updateProjectProgress() {
        int projectProgress = calculateProjectProgress();
        setProjectProgress(projectProgress);
    }

    public int calculateChapterProgress(Chapter chapter) {
        int unitCount = chapter.getChunks().size();
        int chapterNumber = chapter.getNumber();
        Map<Integer, Integer> unitsStarted = db.getNumStartedUnitsInProject(project);
        if(unitsStarted.get(chapterNumber) != null) {
            int chapterProgress = calculateProgress(unitsStarted.get(chapterNumber), unitCount);

            return chapterProgress;
        }

        return 0;
    }

    public void setChapterProgress(Chapter chapter, int progress) {
        try {
            int chapterId = db.getChapterId(project, chapter.getNumber());
            db.setChapterProgress(chapterId, progress);
        } catch (IllegalArgumentException e) {
            Logger.i(this.toString(), e.getMessage());
        }
    }

    public void updateChapterProgress(Chapter chapter) {
        int chapterProgress = calculateChapterProgress(chapter);
        setChapterProgress(chapter, chapterProgress);
    }

    public void updateChaptersProgress() {
        if(chunks != null) {
            List<Chapter> projectChapters = chunks.getChapters();
            for (Chapter chapter: projectChapters) {
                updateChapterProgress(chapter);
            }
        }
    }

    private Chapter getChapter(int chapterNumber) {
        if(chunks != null) {
            List<Chapter> chapters = chunks.getChapters();
            for (Chapter chapter: chapters) {
                if(chapter.getNumber() == chapterNumber) {
                    return chapter;
                }
            }
        }

        return null;
    }

    private int calculateProgress(int current, int total) {
        return Math.round(((float) current / total) * 100);
    }

    public void destroy() {
        db.close();
        db = null;
        project = null;
        context = null;
    }
}