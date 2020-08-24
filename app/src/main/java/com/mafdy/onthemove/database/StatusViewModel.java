package com.mafdy.onthemove.database;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;
import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.List;

/**
 * Created by SBP on 7/12/2018.
 */

public class StatusViewModel extends AndroidViewModel {


    private LiveData<List<Status>> mAllStatusOrderByDateTime;
    private LiveData<Status> mLatestStatus;
    private AppDatabase database;

    public StatusViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(application);
        Calendar from = Calendar.getInstance();
        from.set(Calendar.DAY_OF_MONTH,from.get(Calendar.DAY_OF_MONTH) - 1);
        Calendar to = Calendar.getInstance();
        mAllStatusOrderByDateTime = database.status().getAllStatusOrderByDatetime(from.getTimeInMillis(),to.getTimeInMillis());
        mLatestStatus = database.status().getLatestStatus();

    }


    public LiveData<List<Status>> getAllStatusOrderByDateTime(Calendar from,Calendar to) {

        mAllStatusOrderByDateTime = database.status().getAllStatusOrderByDatetime(from.getTimeInMillis(),to.getTimeInMillis());
        return mAllStatusOrderByDateTime;
    }

    public LiveData<List<Status>> getCurrentAllStatusOrderByDateTime()
    {
        return mAllStatusOrderByDateTime;
    }


    public LiveData<Status> getLatestStatus() {
        return mLatestStatus;
    }

    public void insert(Status status) {
        new insertAsyncTask(database.status()).execute(status);

    }

    public void delete(Status status) {
        new deleteAsyncTask(database.status()).execute(status);

    }

    public void delete(Calendar from, Calendar to)
    {
        new deleteAsyncTaskList(database.status()).execute(from,to);
    }


    private static class insertAsyncTask extends AsyncTask<Status, Void, Void> {

        private StatusDAO mAsyncTaskDao;

        insertAsyncTask(StatusDAO dao) {
            mAsyncTaskDao = dao;
        }


        @Override
        protected Void doInBackground(com.mafdy.onthemove.database.Status... params) {
            mAsyncTaskDao.insertStatus(params[0]);

            return null;
        }
    };

    private static class deleteAsyncTask extends AsyncTask<Status, Void, Void> {

        private StatusDAO mAsyncTaskDao;

        deleteAsyncTask(StatusDAO dao) {
            mAsyncTaskDao = dao;
        }


        @Override
        protected Void doInBackground(com.mafdy.onthemove.database.Status... params) {
            mAsyncTaskDao.deleteStatus(params[0]);

            return null;
        }
    };

    private static class deleteAsyncTaskList extends AsyncTask<Calendar, Void, Void> {

        private StatusDAO mAsyncTaskDao;

        deleteAsyncTaskList(StatusDAO dao) {
            mAsyncTaskDao = dao;
        }


        @Override
        protected Void doInBackground(Calendar... params) {
            mAsyncTaskDao.deleteStatusList(params[0].getTimeInMillis(),params[1].getTimeInMillis());

            return null;
        }

    };


}
