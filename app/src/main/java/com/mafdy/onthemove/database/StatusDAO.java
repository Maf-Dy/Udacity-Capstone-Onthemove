package com.mafdy.onthemove.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by SBP on 7/6/2018.
 */

@Dao
public interface StatusDAO  {

    @Query("Select * from status where datetime >= :start and datetime <= :end Group By activity,locationaddress  Order By id , latitude , longitude DESC ")
    LiveData<List<Status>> getAllStatusOrderByDatetime(long start, long end);

    @Query("Select * from status where id = ( select max(id) from status)")
    LiveData<Status> getLatestStatus();

    @Query("Select * from status where id = ( select max(id) from status)")
    Status getLatestStatus_Service();

    @Query("Select * from status where locationaddress = '' OR locationaddress is null OR locationaddress = 'null' OR locationaddress = 'Not found'")
    List<Status> getStatusWithEmptyAddress();

    @Insert
    void insertStatus(Status obj);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateStatus(Status obj);

    @Delete
    void deleteStatus(Status obj);

    @Query("Delete from status where datetime >= :start and datetime <= :end")
    void deleteStatusList(long start, long end);

}
