package com.hekkelman.keylocker.activities;

import android.os.Bundle;

import com.hekkelman.keylocker.tasks.UiBasedBackgroundTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

public abstract class BackgroundTaskActivity<Result> extends BaseActivity {
    final protected String TAG_TASK_FRAGMENT = this.getClass().getSimpleName() + ".TaskFragment";

    private ProcessLifecycleObserver mObserver = null;

    abstract void onTaskResult(Result result);

    protected void onReturnToCanceledTask() {
        // This can be overwritten if we need to do something here
    }

    protected void setupUiForTaskState(boolean running) {
        // This can be overwritten when we need to give some UI feedback
    }

    protected boolean cancelTaskOnScreenOff() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (cancelTaskOnScreenOff()) {
            mObserver = new ProcessLifecycleObserver();

            ProcessLifecycleOwner.get().getLifecycle()
                    .addObserver(mObserver);
        }
    }

    @Override
    protected void onDestroy() {
        if (mObserver != null) {
            ProcessLifecycleOwner.get().getLifecycle()
                    .removeObserver(mObserver);

            mObserver = null;
        }

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkBackgroundTask();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // We don't want the task to callback to a dead activity and cause a memory leak, so null it here.
        TaskFragment<Result> taskFragment = findTaskFragment();

        if (taskFragment != null)
            taskFragment.setCallback(null);
    }

    protected void cancelBackgroundTask() {
        TaskFragment<Result> taskFragment = findTaskFragment();

        if (taskFragment != null)
            taskFragment.cancelTask();

        setupUiForTaskState(false);
    }

    protected void startBackgroundTask(UiBasedBackgroundTask<Result> task) {
        TaskFragment<Result> taskFragment = findTaskFragment();

        // Don't start a task if we already have an active task running.
        if (taskFragment == null || taskFragment.isCanceled()) {
            task.setCallback(this::handleTaskResult);

            if (taskFragment == null) {
                taskFragment = new TaskFragment<>();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(taskFragment, TAG_TASK_FRAGMENT)
                        .commit();
            }

            taskFragment.startTask(task);

            setupUiForTaskState(true);
        }
    }

    private void handleTaskResult(Result result) {
        setupUiForTaskState(false);

        onTaskResult(result);

        // Remove the task fragment after the task is finished
        TaskFragment<Result> taskFragment = findTaskFragment();

        if (taskFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(taskFragment)
                    .commit();
        }
    }

    private void checkBackgroundTask() {
        TaskFragment<Result> taskFragment = findTaskFragment();

        if (taskFragment != null) {
            if (taskFragment.isCanceled()) {
                // The task was canceled, so remove the task fragment
                getSupportFragmentManager().beginTransaction()
                        .remove(taskFragment)
                        .commit();

                onReturnToCanceledTask();

                setupUiForTaskState(false);
            } else {
                taskFragment.setCallback(this::handleTaskResult);

                setupUiForTaskState(true);
            }
        } else {
            setupUiForTaskState(false);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private TaskFragment<Result> findTaskFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_TASK_FRAGMENT);

        if (fragment instanceof TaskFragment)
            return (TaskFragment<Result>) fragment;
        else
            return null;
    }

    private class ProcessLifecycleObserver implements DefaultLifecycleObserver {
        @Override
        public void onStop(@NonNull LifecycleOwner owner) {
            if (cancelTaskOnScreenOff())
                cancelBackgroundTask();
        }
    }

    public static class TaskFragment<Result> extends Fragment {
        private UiBasedBackgroundTask<Result> task;

        public TaskFragment() {
            super();
            setRetainInstance(true);
        }

        public void startTask(@NonNull UiBasedBackgroundTask<Result> task) {
            this.task = task;
            task.execute();
        }

        public void setCallback(@Nullable UiBasedBackgroundTask.UiCallback<Result> callback) {
            if (this.task != null)
                this.task.setCallback(callback);
        }

        public boolean isCanceled() {
            if (task != null)
                return task.isCanceled();
            else
                return true;
        }

        public void cancelTask() {
            if (task != null)
                task.cancel();
        }
    }
}
