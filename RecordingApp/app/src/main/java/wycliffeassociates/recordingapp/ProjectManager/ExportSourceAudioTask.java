package wycliffeassociates.recordingapp.ProjectManager;

import com.wycliffeassociates.io.ArchiveOfHolding;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import wycliffeassociates.recordingapp.Reporting.Logger;
import wycliffeassociates.recordingapp.Utils;
import wycliffeassociates.recordingapp.utilities.Task;

/**
 * Created by sarabiaj on 9/23/2016.
 */
public class ExportSourceAudioTask extends Task {

    Project mProject;
    OutputStream mOutput;
    File mBookFolder;
    File mStagingRoot;

    public ExportSourceAudioTask(Project project, File bookFolder, File stagingRoot, OutputStream output){
        mProject = project;
        mOutput = output;
        mStagingRoot = stagingRoot;
        mBookFolder = bookFolder;
    }

    @Override
    public void run() {
        File input = stageFilesForArchive(mProject, mBookFolder, mStagingRoot);
        createSourceAudio(mProject, input, mOutput);
        onTaskProgressUpdateDelegator(75);
        onTaskCompleteDelegator();
    }

    public void createSourceAudio(final Project project, final File input, OutputStream output){
        try {
            final ArchiveOfHolding aoh = new ArchiveOfHolding();
            aoh.createArchiveOfHolding(input, output, true);
        } finally {
            Utils.deleteRecursive(input);
        }
    }

    private File stageFilesForArchive(Project project, File input, File stagingRoot){
        File root = new File(stagingRoot, project.getTargetLanguage() + Utils.capitalizeFirstLetter(project.getSlug()));
        File lang = new File(root, project.getTargetLanguage());
        File version = new File(lang, project.getSource());
        File book = new File(version, project.getSlug());
        book.mkdirs();
        for(File c : input.listFiles()){
            if(c.isDirectory()){
                File chapter = new File(book, c.getName());
                chapter.mkdir();
                for(File f : c.listFiles()){
                    if(!f.isDirectory()) {
                        try {
                            FileUtils.copyFileToDirectory(f, chapter);
                        } catch (IOException e) {
                            Logger.e(this.toString(), "IOException staging files for archive", e);
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return root;
    }
}
