package com.hekkelman.keylocker.tasks;

import android.content.Context;

import com.hekkelman.keylocker.R;
import com.hekkelman.keylocker.datamodel.KeyDb;
import com.hekkelman.keylocker.datamodel.KeyDbException;
import com.hekkelman.keylocker.datamodel.Note;

import androidx.annotation.NonNull;

public class SaveNoteTask extends UiBasedBackgroundTask<SaveNoteTask.Result> {

    private final Note note;
    private final String name;
    private final String text;
    private final boolean finishOnSaved;

    public SaveNoteTask(Context context, Note note, String name, String text, boolean finishOnSaved) {
        super(Result.failure(context.getString(R.string.save_note_task_cancelled)));

        this.note = note;
        this.name = name;
        this.text = text;
        this.finishOnSaved = finishOnSaved;
    }

    //    @Override
    @NonNull
    protected Result doInBackground() {
        return saveNote();
    }

    @NonNull
    private Result saveNote() {
        try {
            KeyDb.setNote(note, this.name, this.text);
            return Result.success(this.finishOnSaved);
        } catch (KeyDbException exception) {
            return Result.failure(exception.getMessage());
        }
    }

    public static class Result {
        public final boolean saved;
        public final boolean finish;
        public final String errorMessage;

        public Result(boolean saved, boolean finish, String errorMessage) {
            this.saved = saved;
            this.finish = finish;
            this.errorMessage = errorMessage;
        }

        public static Result success(boolean finish) {
            return new Result(true, finish, "");
        }

        public static Result failure(String errorMessage) {
            return new Result(false, false, errorMessage);
        }
    }
}
