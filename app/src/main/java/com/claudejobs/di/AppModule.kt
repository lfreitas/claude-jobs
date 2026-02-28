package com.claudejobs.di

import android.content.Context
import androidx.room.Room
import com.claudejobs.data.db.AppDatabase
import com.claudejobs.data.db.TaskDao
import com.claudejobs.data.db.TaskRunDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "claude_jobs.db").build()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideTaskRunDao(db: AppDatabase): TaskRunDao = db.taskRunDao()
}
